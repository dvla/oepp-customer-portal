package controllers;

import ch.qos.logback.core.joran.conditional.ElseAction;
import configuration.inject.ConfigurationValue;
import play.i18n.Messages;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import scala.concurrent.duration.Duration;
import session.aop.DestroySessionAfterwards;
import session.aop.RequireSession;
import session.SessionManager;
import uk.gov.dvla.domain.Offence;

import javax.inject.Inject;

public class ErrorController extends Controller {

    @Inject
    private SessionManager sessionManager;

    @Inject
    @ConfigurationValue(key = "securityService.keyExpiry")
    private String keyExpiry;

    @DestroySessionAfterwards
    public Result displayCaseNotFoundErrorPage() {
        Logger.debug("Displaying case not found error page");
        return ok(views.html.pages.errors.business.caseNotFound.render());
    }

    @DestroySessionAfterwards
    public Result displayVehicleRegistrationMarkMismatchErrorPage() {
        Logger.debug("Displaying vehicle registration mismatch error page");
        return ok(views.html.pages.errors.business.vehicleRegistrationMarkMismatch.render());
    }

    @DestroySessionAfterwards
    public Result displayNotSupportedCaseTypeErrorPage() {
        Logger.debug("Displaying not supported case type error page");
        return ok(views.html.pages.errors.business.cannotPayOnline.render());
    }

    @RequireSession(withOffence = true)
    @DestroySessionAfterwards
    public Result displayPaymentAlreadyMadeErrorPage() {
        Logger.debug("Displaying payment already made error page");
        Offence offence = sessionManager.session().offence().get();
        return ok(views.html.pages.errors.business.paymentAlreadyMade.render(offence.getCriteria()));
    }

    @RequireSession(withOffence = true)
    @DestroySessionAfterwards
    public Result displayNoPaymentRequiredErrorPage() {
        Logger.debug("Displaying no payment required error page");
        Offence offence = sessionManager.session().offence().get();
        return ok(views.html.pages.errors.business.noPaymentRequired.render(offence.getCriteria()));
    }

    @RequireSession
    @DestroySessionAfterwards
    public Result displayCaseWithDebtCollectorsErrorPage() {
        Logger.debug("Displaying case with debt collectors error page");
        return ok(views.html.pages.errors.business.caseWithDebtCollectors.render());
    }

    @RequireSession
    @DestroySessionAfterwards
    public Result displayCannotPayOnlineErrorPage() {
        Logger.debug("Displaying cannot pay online error page");
        return ok(views.html.pages.errors.business.cannotPayOnline.render());
    }

    @RequireSession(withOffence = true, withPayment = true)
    public Result displayPaymentNotAuthorisedErrorPage() {
        Logger.debug("Displaying payment not authorised error page");
        return ok(views.html.pages.errors.business.paymentNotAuthorised.render());
    }

    @RequireSession(withOffence = true)
    @DestroySessionAfterwards
    public Result displayProsecutionPage() {
        Logger.debug("Displaying case with prosecution error page");
        Offence offence = sessionManager.session().offence().get();
        if ("144A".equals(offence.getCaseRejection().get().getCaseType()))
            if ("Y".equals(offence.getCaseRejection().get().getcaseInvalid()))
                return ok(views.html.pages.errors.business.prosecution.render(
                        Messages.get("view.prosecution.header.cie"),
                        Messages.get("view.prosecution.p1.cie.invalid")
                ));
            else
                return ok(views.html.pages.errors.business.prosecution.render(
                        Messages.get("view.prosecution.header.cie"),
                        Messages.get("view.prosecution.p1.cie")
            ));
        else
            return ok(views.html.pages.errors.business.prosecution.render(
                  Messages.get("view.prosecution.header"),
                  Messages.get("view.prosecution.p1")
            ));
    }

    @RequireSession(withOffence = true)
    @DestroySessionAfterwards
    public Result displayPaymentErrorPage() {
        Logger.debug("Displaying payment error page");
        return ok(views.html.pages.errors.business.paymentError.render());
    }

    @DestroySessionAfterwards
    public Result displayServiceUnavailableErrorPage() {
        Logger.debug("Displaying service unavailable error page");
        return ok(views.html.pages.errors.technical.serviceUnavailable.render());
    }

    @DestroySessionAfterwards
    @RequireSession(withLockedDate = true)
    public Result displayPenaltyDetailsLockedErrorPage() {
        Logger.debug("Displaying penalty details locked error page");
        Long keyExpiryMins = Duration.create(keyExpiry).toMinutes();
        return ok(views.html.pages.errors.business.penaltyDetailsLocked.render(sessionManager.session().lockedDate().get(), keyExpiryMins));
    }

    public Result displayPageNotFoundErrorPage() {
        Logger.debug("Displaying page not found error page");
        return ok(views.html.pages.errors.technical.pageNotFound.render());
    }

}
