package controllers;

import auditing.AuditService;
import configuration.inject.ConfigurationValue;
import models.Payment;
import models.PaymentReceipt;
import models.form.CaseSearchModel;
import models.form.PaymentReceiptModel;
import models.form.PenaltyModel;
import models.form.ConfirmationUntaxedModel;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.joda.time.DateTime;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.data.validation.ValidationError;
import play.filters.csrf.CSRFConfig;
import play.i18n.Messages;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.twirl.api.Html;
import services.OffenceServiceClient;
import services.PaymentServiceClient;
import services.SecurityServiceClient;
import services.email.EmailServiceClient;
import services.email.model.AddressType;
import services.email.model.Email;
import services.result.CheckSecurityResult;
import services.result.GetCaseResult;
import services.result.InitiatePaymentResult;
import services.result.TransactionResult;
import session.SessionManager;
import session.aop.DestroySessionAfterwards;
import session.aop.RequireSession;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentRequest;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentResponse;
import utils.ActionBuilder;
import utils.URLParser;
import utils.EVL;
import views.models.EVLView;
import views.ViewFunctions;

import javax.inject.Inject;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static auditing.AuditMessageFactory.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static models.PaymentReceipt.receiptNotRequested;
import static models.PaymentReceipt.receiptRequestedForEmail;
import static org.joda.time.DateTime.now;

public class ApplicationController extends Controller {

    private final String receiptSender;
    private final String feedbackEmailAddress;
    private final boolean bruteForceCheckingEnabled;
    private final String startPageURI;
    private final boolean evlEnabled;

    @Inject
    private CSRFConfig tokenConfig;
    @Inject
    private HttpExecutionContext httpExecutionContext;
    @Inject
    private FormFactory formFactory;

    @Inject
    private SessionManager sessionManager;
    @Inject
    private OffenceServiceClient offenceServiceClient;
    @Inject
    private PaymentServiceClient paymentServiceClient;
    @Inject
    private EmailServiceClient emailServiceClient;
    @Inject
    private SecurityServiceClient securityServiceClient;
    @Inject
    private AuditService auditService;
    @Inject
    private EVL evl;

    @Inject
    public ApplicationController(@ConfigurationValue(key = "email.receipt.sender") String receiptSender,
                                 @ConfigurationValue(key = "email.feedback.recipient") String feedbackEmailAddress,
                                 @ConfigurationValue(key = "securityService.bruteForceCheckingEnabled") boolean bruteForceCheckingEnabled,
                                 @ConfigurationValue(key = "startPageURI") String startPageURI,
                                 @ConfigurationValue(key = "evl.enabled") boolean evlEnabled) {
        checkArgument(!isNullOrEmpty(receiptSender), "Receipt sender is required");
        checkArgument(!isNullOrEmpty(feedbackEmailAddress), "Feedback recipient is required");
        checkArgument(!isNullOrEmpty(startPageURI), "Start page URI is required");

        EmailValidator emailValidator = EmailValidator.getInstance(true);
        checkArgument(emailValidator.isValid(receiptSender), "Receipt sender is invalid");
        checkArgument(emailValidator.isValid(feedbackEmailAddress), "Feedback recipient is invalid");

        if (!startPageURI.startsWith("/")) { // then it must be an URL
            UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});
            checkArgument(urlValidator.isValid(startPageURI), "Start page URL is invalid");
        }

        this.receiptSender = receiptSender;
        this.feedbackEmailAddress = feedbackEmailAddress;
        this.bruteForceCheckingEnabled = bruteForceCheckingEnabled;
        this.startPageURI = startPageURI;
        this.evlEnabled = evlEnabled;
    }

    // GET
    @DestroySessionAfterwards
    public Result displayThankYouPage() {
        Logger.debug("Displaying 'thank you' page");
        return ok((Html) views.html.thanks.render());
    }

    // GET
    public Result redirectToStartPage() {
        Logger.debug("Redirecting to start page: {}", startPageURI);
        return redirect(startPageURI);
    }

    // GET
    @DestroySessionAfterwards
    public Result displaySearchCaseForm() {
        Logger.debug("Displaying search case form");
        return ok(views.html.search.render(formFactory.form(CaseSearchModel.class)));
    }

    // POST
    public CompletionStage<Result> onCaseSearch() {
        Form<CaseSearchModel> form = formFactory.form(CaseSearchModel.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.debug("Displaying search case with error messages");
            return CompletableFuture.completedFuture(badRequest((Html) views.html.search.render(form)));
        }

        CaseSearchModel formData = form.get();

        if (bruteForceCheckingEnabled) {
            return securityServiceClient.checkForBruteForceAttack(formData.getCaseNumber(), formData.getVehicleRegistrationMark()).thenComposeAsync(result -> {
                if (result instanceof CheckSecurityResult.Success) {
                    Logger.debug("Check for brute force attack has returned OK for case: {}", formData);
                    return searchCase(formData, sessionManager.session(), true);
                } else if (result instanceof CheckSecurityResult.Forbidden) {
                    Logger.warn("Possible brute force attack for case: {}", formData);
                    sessionManager.session().setLockedDate(DateTime.now().toDate());
                    return CompletableFuture.completedFuture(redirect(routes.ErrorController.displayPenaltyDetailsLockedErrorPage()));
                } else {
                    Logger.error("There has been an error checking for a brute force attack for case: {}", formData);
                    return CompletableFuture.completedFuture(redirect(routes.ErrorController.displayServiceUnavailableErrorPage()));
                }
            }, httpExecutionContext.current());
        } else {
            Logger.warn("Brute force checking is disabled");
            return searchCase(formData, sessionManager.session(), false);
        }
    }

    private CompletionStage<Result> searchCase(CaseSearchModel formData, SessionManager.Session session, boolean bruteForceCheckingEnabled) {
        return offenceServiceClient.getCase(formData.getCaseNumber(), formData.getVehicleRegistrationMark(), session.id()).thenApplyAsync(result -> {
            if (result instanceof GetCaseResult.Found) {
                GetCaseResult.Found foundResult = (GetCaseResult.Found) result;
                session.setOffence(foundResult.getResponse());

                auditService.sendAuditMessage(() -> createAuditMessageOnCaseFound(foundResult));

                if (bruteForceCheckingEnabled) {
                    securityServiceClient.deleteTokensUsedToCheckForBruteForceAttack(formData.getCaseNumber(), formData.getVehicleRegistrationMark());
                    sessionManager.session().removeLockedDate();
                }
            } else {
                auditService.sendAuditMessage(() -> createAuditMessageOnCaseNotFound(result, formData.getCaseNumber(), formData.getVehicleRegistrationMark()));
            }
            return result.toAction();
        }, httpExecutionContext.current());
    }

    // GET
    @RequireSession(withOffence = true)
    public Result displayPenaltySummaryPage() {
        Logger.debug("Displaying penalty summary page");
        return ok(views.html.penalty.render(sessionManager.session().offence().get(), formFactory.form(PenaltyModel.class)));
    }

    // POST
    @RequireSession(withOffence = true)
    public Result onPenaltyAcceptance() {
        Offence offence = sessionManager.session().offence().get();
        if (!offence.getVehicleData().isTaxed() || "144A".equals(offence.getCaseData().get().getCaseType())) {
            Form<PenaltyModel> form = formFactory.form(PenaltyModel.class).bindFromRequest();

            if (form.hasErrors()) {
                Logger.debug("Displaying penalty summary page with validation errors");
                return badRequest(views.html.penalty.render(offence, form));
            }
        }

        auditService.sendAuditMessage(() -> createAuditMessagePenaltyAccepted(offence));

        Logger.debug("Redirecting to receipt selection form");
        return redirect(routes.ApplicationController.displayReceiptSelectionForm());
    }


    // POST
    public Result onOptSelection() {
        Logger.debug("Displaying IBM Digitial Analytics");
        return ok(views.html.pages.help.ibmDigitalAnalytics.render());

    }


    // GET
    @RequireSession(withOffence = true)
    public Result displayReceiptSelectionForm() {
        Logger.debug("Displaying receipt selection page");

        Form<PaymentReceiptModel> form = sessionManager.session().paymentReceipt()
                .map(value -> formFactory.form(PaymentReceiptModel.class).fill(new PaymentReceiptModel(value.isReceiptRequested(), value.getUserEmail())))
                .orElse(formFactory.form(PaymentReceiptModel.class));

        return ok(views.html.receipt.render(form));
    }


    // POST
    @RequireSession(withOffence = true)
    public CompletionStage<Result> onReceiptSelection() {
        Form<PaymentReceiptModel> form = formFactory.form(PaymentReceiptModel.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.debug("Displaying receipt selection page with validation errors");
            return CompletableFuture.completedFuture(badRequest((Html) views.html.receipt.render(form)));
        }

        PaymentReceiptModel formData = form.get();
        if (formData.getEmailReceiptDecision()) {
            sessionManager.session().setPaymentReceipt(receiptRequestedForEmail(formData.getReceiptEmails().getEmail()));
        } else {
            sessionManager.session().setPaymentReceipt(receiptNotRequested());
        }

        return initPayment();
    }

    //POST
    @RequireSession(withOffence = true)
    @DestroySessionAfterwards
    public Result onWhatNextSelection(){
        SessionManager.Session session = sessionManager.session();
        Offence offence = session.offence().get();

        Form<ConfirmationUntaxedModel> form = formFactory.form(ConfirmationUntaxedModel.class).bindFromRequest();
        if (form.hasErrors()) {
            Logger.debug("Displaying confirmation page with validation errors");
            return Results.badRequest((Html)views.html.confirmation.render(offence,
                    session.paymentReceipt().get(),
                    session.payment().get(),
                    form));
        }
        ConfirmationUntaxedModel formData = form.get();
        return onValidatedWhatNextSelection(formData, form);
    }

    public Result onValidatedWhatNextSelection(ConfirmationUntaxedModel formData, Form<ConfirmationUntaxedModel> form) {
        String whatNextURL = "";
        SessionManager.Session session = sessionManager.session();
        Offence offence = session.offence().get();

        if(formData.getWhatNextDecision().equals("completed")) {
            Logger.debug("Displaying 'thank you' page");
            return redirect(routes.ApplicationController.displayThankYouPage());
        }

        if (formData.getWhatNextDecision().equals("sold")) {
            whatNextURL = "https://www.gov.uk/sold-bought-vehicle";
            return redirect(whatNextURL);
        }

        if (evlEnabled && !offence.getVehicleData().isTaxed()) {
            Optional<Result> evlViewResult = offence.getVehicleData().getV5Reference().map(v5Reference -> {
                try {
                    Map<String, String> postFields = new HashMap<>();
                    postFields.put("vrm", offence.getCriteria().getVehicleRegistrationMark());
                    postFields.put("v5", v5Reference);

                    String whatNextURLLink = "";
                    if (formData.getWhatNextDecision().equals("tax")) {
                        whatNextURLLink = evl.getTaxURL() + "?o=1&l=" + ViewFunctions.userLocale() + "-GB";
                    } else if (formData.getWhatNextDecision().equals("sorn")) {
                        whatNextURLLink = evl.getSORNURL() + "?o=1&l=" + ViewFunctions.userLocale() + "-GB";
                    } else {
                        return Results.badRequest(views.html.confirmation.render(offence,
                                session.paymentReceipt().get(),
                                session.payment().get(),
                                form));
                    }

                    EVLView evlViewWithLink = new EVLView(evl, evl.encryptFields(postFields), whatNextURLLink);
                    return ok(views.html.evllink.render(evlViewWithLink));

                } catch (NullPointerException ex) {
                    // Shared key not setup in configuration
                    return null;
                } catch (Exception ex) {
                    Logger.error("Cannot encrypt data for EVL", ex);
                    return null;
                }
            });

            if (evlViewResult.isPresent()) {
                return evlViewResult.get();
            } else {
                if (formData.getWhatNextDecision().equals("tax")) {
                    whatNextURL = Messages.get("view.confirmation.whatNext.taxVehicle.link");
                } else if (formData.getWhatNextDecision().equals("sorn")) {
                    whatNextURL = Messages.get("view.confirmation.whatNext.sornVehicle.link");
                } else {
                    return Results.badRequest((Html) views.html.confirmation.render(offence,
                            session.paymentReceipt().get(),
                            session.payment().get(),
                            form));
                }
                return redirect(whatNextURL);
            }
        } else {
            if (formData.getWhatNextDecision().equals("tax")) {
                whatNextURL = Messages.get("view.confirmation.whatNext.taxVehicle.link");
            } else if (formData.getWhatNextDecision().equals("sorn")) {
                whatNextURL = Messages.get("view.confirmation.whatNext.sornVehicle.link");
            } else {
                return Results.badRequest((Html) views.html.confirmation.render(offence,
                        session.paymentReceipt().get(),
                        session.payment().get(),
                        form));
            }
            return redirect(whatNextURL);
        }
    }

    // POST
    @RequireSession(withOffence = true)
    public CompletionStage<Result> initPayment() {
        SessionManager.Session session = sessionManager.session();

        Offence offence = session.offence().get();
        InitiateOffencePaymentRequest paymentRequest = new InitiateOffencePaymentRequest.Builder()
                .setTransactionID(offence.getCriteria().getCaseNumber().toString())
                .setPaymentAmount(offence.getCaseData().get().getPaymentAmount())
                .setLanguage(lang().code().toUpperCase())
                .setPostAuthorizeCallbackURL(buildPostAuthorizeCallbackURL())
                .create();

        return paymentServiceClient.initiatePayment(paymentRequest).thenApplyAsync(result -> {
            if (result instanceof InitiatePaymentResult.Success) {
                InitiateOffencePaymentResponse response = ((InitiatePaymentResult.Success) result).getResponse();
                session.setPayment(new Payment(response.getPaymentID(), URLParser.parse(response.getPaymentPageUrl())));
            }

            auditService.sendAuditMessage(() -> createAuditMessagePaymentInitiation(result, offence, session.paymentReceipt()));
            return result.toAction();
        });
    }

    private URL buildPostAuthorizeCallbackURL() {
        return new ActionBuilder().withToken(tokenConfig).withSessionID().build(routes.ApplicationController.fulfillTransaction());
    }

    // GET
    @RequireSession(withOffence = true, withPayment = true)
    public Result displayPaymentForm() {
        Logger.debug("Displaying payment form");

        SessionManager.Session session = sessionManager.session();

        boolean receiptRequested = session.paymentReceipt().map(PaymentReceipt::isReceiptRequested).orElse(false);
        return ok(views.html.payment.render(session.payment().get().getFormURL(), receiptRequested));
    }

    // GET & POST
    @RequireSession(withOffence = true, withPayment = true)
    public CompletionStage<Result> fulfillTransaction() {
        Http.Context.current().session().put("id", request().getQueryString("session"));
        SessionManager.Session session = sessionManager.session();

        Payment payment = session.payment().get();

        return paymentServiceClient.fulfillTransaction(payment.getId()).thenApplyAsync(transactionResult -> {
            Offence offence = session.offence().get();

            if (transactionResult instanceof TransactionResult.Success) {
                TransactionResult.Success transactionSuccess = (TransactionResult.Success) transactionResult;
                session.setPayment(payment.setPaidDate(Optional.of(now().toDate())));

                session.paymentReceipt().ifPresent(paymentReceipt -> {
                    if (paymentReceipt.isReceiptRequested()) {
                        try {
                            emailServiceClient.sendEmail(new Email.Builder()
                                    .fromSender(receiptSender)
                                    .toRecipient(AddressType.TO, paymentReceipt.getUserEmail())
                                    .withSubject(receiptSubject(offence))
                                    .withHtmlBody(receiptHtmlBody(offence, payment.getPaidDate().get(), paymentReceipt.getUserEmail()))
                                    .create()
                            );

                            auditService.sendAuditMessage(() -> createAuditMessageTransactionSuccessful(transactionSuccess, offence, session.paymentReceipt(), payment.getId(), "SENT"));
                        } catch (Exception ex) {
                            Logger.error("Cannot send receipt email", ex);
                            auditService.sendAuditMessage(() -> createAuditMessageTransactionSuccessful(transactionSuccess, offence, session.paymentReceipt(), payment.getId(), "FAILED"));
                        }

                    } else {
                        auditService.sendAuditMessage(() -> createAuditMessageTransactionSuccessful(transactionSuccess, offence, session.paymentReceipt(), payment.getId(), "NOT_REQUESTED"));
                    }
                });

                offenceServiceClient.closeCase(payment.getId(), offence.getCriteria().getCaseNumber(), offence.getCaseData().get(), session.id());
            } else {
                auditService.sendAuditMessage(() -> createAuditMessageTransactionError(transactionResult, offence, session.paymentReceipt(), payment.getId()));
            }

            return transactionResult.toAction();
        }, httpExecutionContext.current());
    }

    private String receiptSubject(Offence offence) {
        return Messages.get("email.receipt.subject", offence.getCriteria().getVehicleRegistrationMark());

    }

    private String receiptHtmlBody(Offence offence, Date paidDate, String userEmailAddress) {
        return views.html.email.receipt.render(offence, paidDate, userEmailAddress, feedbackEmailAddress).body();
    }

    // GET
    @RequireSession(withOffence = true, withPaymentReceipt = true, withPayment = true, withPaymentDate = true)
    public Result displayPaymentConfirmation() {
        Logger.debug("Displaying payment confirmation page");

        SessionManager.Session session = sessionManager.session();

        Offence offence = session.offence().get();


        return ok(views.html.confirmation.render(
                offence,
                session.paymentReceipt().get(),
                session.payment().get(),
                formFactory.form(ConfirmationUntaxedModel.class)));
    }

    // GET
    public Result displayCookies() {
        Logger.debug("Displaying cookies page");
        return ok(views.html.pages.help.cookies.render());
    }

    // GET
    public Result displayibmDigitalAnalytics() {
        Logger.debug("Displaying IBM Digitial Analytics");
        return ok(views.html.pages.help.ibmDigitalAnalytics.render());
    }


    // GET
    public Result displayTermsAndConditions() {
        Logger.debug("Displaying terms and conditions page");
        return ok(views.html.pages.help.termsAndConditions.render());
    }

}
