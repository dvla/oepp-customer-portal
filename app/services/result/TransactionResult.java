package services.result;

import play.Logger;
import play.mvc.Result;
import auditing.AuditableResult;
import services.result.base.ServiceResult;

import static play.mvc.Results.redirect;

public interface TransactionResult extends ServiceResult, AuditableResult {

    class Success implements TransactionResult {

        @Override
        public Result toAction() {
            Logger.debug("Redirecting to payment confirmation page");
            return redirect(controllers.routes.ApplicationController.displayPaymentConfirmation());
        }

        @Override
        public String getPageMovement() {
            return "paymentPageToPaymentConfirmationPage";
        }
    }

    class NotAuthorised implements TransactionResult {

        @Override
        public Result toAction() {
            Logger.debug("Redirecting to payment not authorised error page");
            return redirect(controllers.routes.ErrorController.displayPaymentNotAuthorisedErrorPage());
        }

        @Override
        public String getPageMovement() {
            return "paymentPageToPaymentNotAuthorisedErrorPage";
        }
    }

    class Error implements TransactionResult {

        @Override
        public Result toAction() {
            Logger.error("Redirecting to payment error page");
            return redirect(controllers.routes.ErrorController.displayPaymentErrorPage());
        }

        @Override
        public String getPageMovement() {
            return "paymentPageToPaymentErrorPage";
        }
    }

}
