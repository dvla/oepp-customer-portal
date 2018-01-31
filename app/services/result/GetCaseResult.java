package services.result;

import play.Logger;
import play.mvc.Result;
import auditing.AuditableResult;
import services.result.base.ServiceResult;
import uk.gov.dvla.domain.Offence;

import static play.mvc.Results.redirect;

public interface GetCaseResult extends ServiceResult, AuditableResult {

    class Found implements GetCaseResult {
        private final Offence response;

        public Found(Offence response) {
            this.response = response;
        }

        public Offence getResponse() {
            return response;
        }

        @Override
        public Result toAction() {
            if (response.getCaseRejection().isPresent()) {
                switch (response.getCaseRejection().get().getRejectionReason()) {
                    case INVALID_STATE:
                        Logger.debug("Case {} has invalid state - redirecting back to cannot pay online error page", response.getCriteria().getCaseNumber());
                        return redirect(controllers.routes.ErrorController.displayCannotPayOnlineErrorPage());
                    case NO_PAYMENT_REQUIRED:
                        Logger.debug("Case {} doesn't require payment - redirecting back to no payment required error page", response.getCriteria().getCaseNumber());
                        return redirect(controllers.routes.ErrorController.displayNoPaymentRequiredErrorPage());
                    case PENALTY_ALREADY_PAID:
                        Logger.debug("Case {} has been already paid - redirecting back to payment already made error page", response.getCriteria().getCaseNumber());
                        return redirect(controllers.routes.ErrorController.displayPaymentAlreadyMadeErrorPage());
                    case PASSED_TO_DEBT_COLLECTORS:
                        Logger.debug("Case {} has been passed to debt collectors - redirecting back to case with debt collectors error page", response.getCriteria().getCaseNumber());
                        return redirect(controllers.routes.ErrorController.displayCaseWithDebtCollectorsErrorPage());
                    case PASSED_TO_COURT:
                        Logger.debug("Case {} has been passed to court - redirecting back to case with court prosecution page", response.getCriteria().getCaseNumber());
                        return redirect(controllers.routes.ErrorController.displayProsecutionPage());
                    default:
                        throw new RuntimeException("Unknown case closure");
                }
            } else {
                Logger.debug("Redirecting to penalty summary page");
                return redirect(controllers.routes.ApplicationController.displayPenaltySummaryPage());
            }
        }

        @Override
        public String getPageMovement() {
            return response.getCaseRejection().map(caseRejection -> {
                switch (response.getCaseRejection().get().getRejectionReason()) {
                    case INVALID_STATE:
                        return "enterDetailsToCannotPayOnlineErrorPage";
                    case NO_PAYMENT_REQUIRED:
                        return "enterDetailsToNoPaymentRequiredErrorPage";
                    case PENALTY_ALREADY_PAID:
                        return "enterDetailsToPaymentAlreadyMadeErrorPage";
                    case PASSED_TO_DEBT_COLLECTORS:
                        return "enterDetailsToCaseWithDebtCollectorsErrorPage";
                    case PASSED_TO_COURT:
                        return "enterDetailsToCaseWithCourtProsecutionPage";
                    default:
                        throw new RuntimeException("Unknown case rejection returned from get case result");
                }
            }).orElse("enterDetailsToPenaltySummary");
        }
    }

    class NotFound implements GetCaseResult {
        @Override
        public Result toAction() {
            Logger.debug("Redirecting back to case not found error page");
            return redirect(controllers.routes.ErrorController.displayCaseNotFoundErrorPage());
        }

        @Override
        public String getPageMovement() {
            return "enterDetailsToCaseNotFoundErrorPage";
        }
    }

    class VehicleRegistrationMarkMismatch implements GetCaseResult {
        @Override
        public Result toAction() {
            Logger.debug("Redirecting back to vehicle registration mark mismatch error page");
            return redirect(controllers.routes.ErrorController.displayVehicleRegistrationMarkMismatchErrorPage());
        }

        @Override
        public String getPageMovement() {
            return "enterDetailsToVehicleRegistrationMarkMismatchErrorPage";
        }
    }

    class NotSupportedCaseType implements GetCaseResult {
        @Override
        public Result toAction() {
            Logger.warn("Redirecting back to not supported case type error page");
            return redirect(controllers.routes.ErrorController.displayCannotPayOnlineErrorPage());
        }

        @Override
        public String getPageMovement() {
            return "enterDetailsToCannotPayOnlineErrorPage";
        }
    }

    class Error implements GetCaseResult {
        @Override
        public Result toAction() {
            Logger.error("Redirecting to service unavailable page");
            return redirect(controllers.routes.ErrorController.displayServiceUnavailableErrorPage());
        }

        @Override
        public String getPageMovement() {
            return "enterDetailsToServiceUnavailableErrorPage";
        }
    }

}
