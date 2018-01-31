package controllers;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import services.OffenceServiceClient;
import services.PaymentServiceClient;
import services.email.EmailServiceClient;
import services.email.model.AddressType;
import services.email.model.Email;
import services.result.CloseCaseResult;
import services.result.GetCaseResult;
import services.result.InitiatePaymentResult;
import services.result.TransactionResult;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.domain.data.CaseRejection.CaseRejectionReason;
import uk.gov.dvla.domain.data.VehicleData;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static controllers.constants.OffenceConstants.*;
import static controllers.constants.PaymentConstants.*;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static models.PaymentReceipt.receiptNotRequested;
import static models.PaymentReceipt.receiptRequestedForEmail;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.noMoreInteractions;

public class ApplicationControllerTest extends BaseStatefulControllerTest {

    private final OffenceServiceClient offenceServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final EmailServiceClient emailServiceClient;

    public ApplicationControllerTest() {
        offenceServiceClient = mock(OffenceServiceClient.class);
        paymentServiceClient = mock(PaymentServiceClient.class);
        emailServiceClient = mock(EmailServiceClient.class);

        builder = builder.configure(ImmutableMap.<String, Object>builder()
                .put("email.receipt.sender", "noreply@dvla.gov.uk")
                .put("email.feedback.recipient", "feedback@dvla.gov.uk")
                .build()
        ).overrides(new AbstractModule() {
            @Override
            protected void configure() {
                bind(EmailServiceClient.class).toInstance(emailServiceClient);
                bind(OffenceServiceClient.class).toInstance(offenceServiceClient);
                bind(PaymentServiceClient.class).toInstance(paymentServiceClient);
            }
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToPenaltySummaryPage() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.Found(offence())));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ApplicationController.displayPenaltySummaryPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenCaseTypeIsNotSupported() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.NotSupportedCaseType()));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayCannotPayOnlineErrorPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenCaseStateIsInvalid() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.Found(offence(CaseRejectionReason.INVALID_STATE))));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayCannotPayOnlineErrorPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenNoPaymentIsRequired() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.Found(offence(CaseRejectionReason.NO_PAYMENT_REQUIRED))));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayNoPaymentRequiredErrorPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenPenaltyHasBeenAlreadyPaid() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.Found(offence(CaseRejectionReason.PENALTY_ALREADY_PAID))));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayPaymentAlreadyMadeErrorPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenCaseHasBeenPassedToDebtCollectors() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.Found(offence(CaseRejectionReason.PASSED_TO_DEBT_COLLECTORS))));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayCaseWithDebtCollectorsErrorPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenCaseHasBeenPassedToCourt() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.Found(offence(CaseRejectionReason.PASSED_TO_COURT))));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayProsecutionPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenCaseDoesNotExist() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.NotFound()));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayCaseNotFoundErrorPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToDedicatedErrorPageWhenVehicleRegistrationMarksDoNotMatch() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.VehicleRegistrationMarkMismatch()));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayVehicleRegistrationMarkMismatchErrorPage().url()));
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToServiceUnavailableErrorPageWhenGetCaseError() throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(anyLong(), anyString(), anyString())).thenReturn(completedFuture(new GetCaseResult.Error()));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayServiceUnavailableErrorPage().url()));
        });
    }

    @Test
    public void onReceiptSelection_shouldNotStoreReceiptRecipientEmailInSessionWhenReceiptWasNotRequested() throws MalformedURLException, ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Error()));

        Request request = HTTP.post("/receipt")
                .withFormParameter("emailReceiptDecision", "false")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            verify(session).setPaymentReceipt(receiptNotRequested());
        });
    }

    @Test
    public void onReceiptSelection_shouldStoreReceiptRecipientEmailInSessionWhenReceiptWasRequested() throws MalformedURLException, ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Error()));

        Request request = HTTP.post("/receipt")
                .withFormParameter("emailReceiptDecision", "true")
                .withFormParameter("receiptEmails.email", "user@example.com")
                .withFormParameter("receiptEmails.repeatedEmail", "user@example.com")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> verify(session).setPaymentReceipt(receiptRequestedForEmail("user@example.com")));
    }

    @Test
    public void onReceiptSelection_shouldRedirectToPaymentPageWhenInitiatePaymentIsSuccessful() throws MalformedURLException, ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));

        InitiateOffencePaymentResponse response = new InitiateOffencePaymentResponse.Builder().setPaymentID(PAYMENT_ID).setPaymentReference(PAYMENT_REFERENCE).setPaymentPageUrl(PAYMENT_PAGE_URL).create();

        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Success(response)));

        Request request = HTTP.post("/receipt")
                .withFormParameter("emailReceiptDecision", "true")
                .withFormParameter("receiptEmails.email", "user@example.com")
                .withFormParameter("receiptEmails.repeatedEmail", "user@example.com")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ApplicationController.displayPaymentForm().url()));
        });
    }

    @Test
    public void onReceiptSelection_shouldRedirectToPaymentErrorPageWhenInitiatePaymentIsUnsuccessful() throws MalformedURLException, ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));

        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Error()));

        Request request = HTTP.post("/receipt")
                .withFormParameter("emailReceiptDecision", "true")
                .withFormParameter("receiptEmails.email", "user@example.com")
                .withFormParameter("receiptEmails.repeatedEmail", "user@example.com")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayPaymentErrorPage().url()));
        });
    }

    @Test
    public void initPayment_shouldRedirectToPaymentPageWhenInitiatePaymentIsSuccessful() throws MalformedURLException, ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));

        InitiateOffencePaymentResponse response = new InitiateOffencePaymentResponse.Builder().setPaymentID(PAYMENT_ID).setPaymentReference(PAYMENT_REFERENCE).setPaymentPageUrl(PAYMENT_PAGE_URL).create();

        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Success(response)));

        Request request = HTTP.post("/payment")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ApplicationController.displayPaymentForm().url()));
        });
    }

    @Test
    public void initPayment_shouldRedirectToPaymentErrorPageWhenInitiatePaymentIsUnsuccessful() throws MalformedURLException, ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));

        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Error()));

        Request request = HTTP.post("/payment")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayPaymentErrorPage().url()));
        });
    }

    @Test
    public void fulfillTransaction_shouldStorePaymentDateInSessionWhenPaymentIsSuccessfulAndCloseCaseShouldBeCalled() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.paymentReceipt()).thenReturn(Optional.empty());
        when(session.payment()).thenReturn(Optional.of(payment()));

        when(paymentServiceClient.fulfillTransaction(PAYMENT_ID)).thenReturn(completedFuture(new TransactionResult.Success()));

        Request request = HTTP.post("/payment/finish?" + "session=" + SESSION_ID + "&token=secret")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(session.payment().get().getPaidDate().isPresent(), is(true));
            verify(offenceServiceClient).closeCase(PAYMENT_ID, CASE_NUMBER, caseData(), SESSION_ID);
        });
    }



    @Test
    public void fulfillTransaction_shouldSendEnglishPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsTaxed() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offence(), "en");
        assertCorrectEmailWasSent(actualEmail, "DVLA Receipt for Payment CV07BBB", "/payment-receipt-email.html");
    }

    @Test
    public void fulfillTransaction_shouldSendEnglishPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsUntaxed() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offenceForUntaxedVehicle(), "en");
        assertCorrectEmailWasSent(actualEmail, "DVLA Receipt for Payment CV07BBB", "/payment-receipt-email-untaxed.html");
    }

    @Test
    public void fulfillTransaction_shouldSendEnglishPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsTaxedAndCIE() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offenceForTaxedVehicleCIE(), "en");
        assertCorrectEmailWasSent(actualEmail, "DVLA Receipt for Payment CV07BBB", "/payment-receipt-email.html");
    }

    // This test checks against the 'taxed' payment-receipt-email.html, because CIE cases should not show if a vehicle is untaxed
    @Test
    public void fulfillTransaction_shouldSendEnglishPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsUntaxedAndCIE() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offenceForUntaxedVehicleCIE(), "en");
        assertCorrectEmailWasSent(actualEmail, "DVLA Receipt for Payment CV07BBB", "/payment-receipt-email.html");
    }

    @Test
    public void fulfillTransaction_shouldSendWelshPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsTaxed() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offence(), "cy");
        assertCorrectEmailWasSent(actualEmail, "Derbynneb DVLA am Daliad CV07BBB", "/payment-receipt-email-cy.html");
    }

    @Test
    public void fulfillTransaction_shouldSendWelshPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsUntaxed() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offenceForUntaxedVehicle(), "cy");
        assertCorrectEmailWasSent(actualEmail, "Derbynneb DVLA am Daliad CV07BBB", "/payment-receipt-email-untaxed-cy.html");
    }

    @Test
    public void fulfillTransaction_shouldSendWelshPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsTaxedAndCIE() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offenceForTaxedVehicleCIE(), "cy");
        assertCorrectEmailWasSent(actualEmail, "Derbynneb DVLA am Daliad CV07BBB", "/payment-receipt-email-cy.html");
    }

    // This test checks against the 'taxed' payment-receipt-email.html, because CIE cases should not show if a vehicle is untaxed
    @Test
    public void fulfillTransaction_shouldSendWelshPaymentReceiptEmailWhenUserWishesToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalledAndVehicleIsUntaxedAndCIE() throws IOException, ExecutionException, InterruptedException {
        Email actualEmail = setupMocksAndSendEmail(offenceForUntaxedVehicleCIE(), "cy");
        assertCorrectEmailWasSent(actualEmail, "Derbynneb DVLA am Daliad CV07BBB", "/payment-receipt-email-cy.html");
    }

    private Email setupMocksAndSendEmail(Offence offence, String languageCode) throws ExecutionException, InterruptedException {
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2015-12-07").getMillis());

        when(session.offence()).thenReturn(Optional.of(offence));
        when(session.paymentReceipt()).thenReturn(Optional.of(receiptRequestedForEmail("user@example.com")));
        when(session.payment()).thenReturn(Optional.of(payment()));

        when(paymentServiceClient.fulfillTransaction(PAYMENT_ID)).thenReturn(completedFuture(new TransactionResult.Success()));

        Request request = HTTP.post("/payment/finish?session=" + SESSION_ID + "&token=secret")
                .withLanguage(languageCode)
                .withCSRFToken("secret");

        ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);

        makeRequest(request, (result) -> {
            verify(emailServiceClient).sendEmail(captor.capture());
        });

        return captor.getValue();
    }

    private void assertCorrectEmailWasSent(Email email, String emailSubject, String emailBodyFilePath) throws IOException {
        assertThat(email.getSender(), is("noreply@dvla.gov.uk"));
        assertThat(email.getRecipients().get(AddressType.TO), Matchers.hasItem("user@example.com"));
        assertThat(email.getSubject(), is(emailSubject));
        assertThat(email.getHtmlBody().get(), equalToIgnoringWhiteSpace(getResourceAsString(emailBodyFilePath)));
    }

    private String getResourceAsString(String filePath) throws IOException {
        return Resources.toString(getClass().getResource(filePath), defaultCharset());
    }



    private Offence offenceForUntaxedVehicle() {
        return offenceBuilder()
                .setCaseData(caseData())
                .setVehicleData(new VehicleData.Builder()
                        .setTaxed(false)
                        .setNewTaxStartDate(Optional.of(new DateTime().withYear(2016).withMonthOfYear(5).withDayOfMonth(3)))
                        .create()
                ).create();
    }

    private Offence offenceForUntaxedVehicleCIE() {
        return offenceBuilder()
                .setCaseData(caseDataCIE())
                .setVehicleData(new VehicleData.Builder()
                        .setTaxed(false)
                        .setNewTaxStartDate(Optional.of(new DateTime().withYear(2016).withMonthOfYear(5).withDayOfMonth(3)))
                        .create()
                ).create();
    }

    private Offence offenceForTaxedVehicleCIE() {
        return offenceBuilder()
                .setCaseData(caseDataCIE())
                .setVehicleData(new VehicleData.Builder()
                        .setTaxed(true)
                        .setNewTaxStartDate(Optional.of(new DateTime().withYear(2016).withMonthOfYear(5).withDayOfMonth(3)))
                        .create()
                ).create();
    }

    @Test
    public void fulfillTransaction_shouldNotSendPaymentReceiptEmailWhenUserWishesNotToReceiveOneAndPaymentIsSuccessfulAndCloseCaseShouldBeCalled() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.paymentReceipt()).thenReturn(Optional.empty());
        when(session.payment()).thenReturn(Optional.of(payment())).thenReturn(Optional.of(payment().setPaidDate(Optional.of(new Date()))));
        when(paymentServiceClient.fulfillTransaction(PAYMENT_ID)).thenReturn(completedFuture(new TransactionResult.Success()));

        Request request = HTTP.post("/payment/finish?" + "session=" + SESSION_ID + "&token=secret")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            verify(emailServiceClient, noMoreInteractions()).sendEmail(any());
            verify(offenceServiceClient).closeCase(PAYMENT_ID, CASE_NUMBER, caseData(), SESSION_ID);
        });

    }

    @Test
    public void fulfillTransaction_shouldRedirectToPaymentConfirmationPageWhenPaymentIsSuccessfulAndCloseCaseIsSuccessful() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.paymentReceipt()).thenReturn(Optional.empty());
        when(session.payment()).thenReturn(Optional.of(payment()));
        when(paymentServiceClient.fulfillTransaction(PAYMENT_ID)).thenReturn(completedFuture(new TransactionResult.Success()));
        when(offenceServiceClient.closeCase(anyLong(), anyLong(), any(), anyString())).thenReturn(completedFuture(new CloseCaseResult.Success()));

        Request request = HTTP.post("/payment/finish?" + "session=" + SESSION_ID + "&token=secret")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ApplicationController.displayPaymentConfirmation().url()));
        });
    }

    @Test
    public void fulfillTransaction_shouldRedirectToPaymentConfirmationPageWhenPaymentIsSuccessfulButCloseCaseFails() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.paymentReceipt()).thenReturn(Optional.empty());
        when(session.payment()).thenReturn(Optional.of(payment()));
        when(paymentServiceClient.fulfillTransaction(PAYMENT_ID)).thenReturn(completedFuture(new TransactionResult.Success()));
        when(offenceServiceClient.closeCase(anyLong(), anyLong(), any(), anyString())).thenReturn(completedFuture(new CloseCaseResult.Error()));

        Request request = HTTP.post("/payment/finish?" + "session=" + SESSION_ID + "&token=secret")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ApplicationController.displayPaymentConfirmation().url()));
        });
    }

    @Test
    public void fulfillTransaction_shouldRedirectToPaymentNotAuthorisedErrorPageWhenPaymentIsNotAuthorisedAndCloseCaseShouldNotBeCalled() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.paymentReceipt()).thenReturn(Optional.empty());
        when(session.payment()).thenReturn(Optional.of(payment()));
        when(paymentServiceClient.fulfillTransaction(PAYMENT_ID)).thenReturn(completedFuture(new TransactionResult.NotAuthorised()));

        Request request = HTTP.post("/payment/finish?" + "session=" + SESSION_ID + "&token=secret")
                .withCSRFToken("secret");


        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayPaymentNotAuthorisedErrorPage().url()));
            verify(offenceServiceClient, never()).closeCase(any(), any(), any(), any());
        });

    }

    @Test
    public void fulfillTransaction_shouldRedirectToPaymentErrorPageWhenPaymentGeneralErrorAndCloseCaseShouldNotBeCalled() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.paymentReceipt()).thenReturn(Optional.empty());
        when(session.payment()).thenReturn(Optional.of(payment()));
        when(paymentServiceClient.fulfillTransaction(PAYMENT_ID)).thenReturn(completedFuture(new TransactionResult.Error()));

        Request request = HTTP.post("/payment/finish?" + "session=" + SESSION_ID + "&token=secret")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayPaymentErrorPage().url()));
            verify(offenceServiceClient, never()).closeCase(any(), any(), any(), any());
        });
    }


    @Test
    public void cookiesPageShouldExist() {
        makeRequest(HTTP.get("/help/cookies"), (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void termsAndConditionsPageShouldExist() {
        makeRequest(HTTP.get("/help/terms-and-conditions"), (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void thankYouPageShouldExist() {
        makeRequest(HTTP.get("/thank-you"), (result) -> {
            assertThat(result.status(), is(200));
        });
    }
}