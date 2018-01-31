package services.result;

import controllers.routes;
import play.Logger;
import play.mvc.Result;
import auditing.AuditableResult;
import services.result.base.ServiceResult;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentResponse;

import static play.mvc.Results.redirect;

public interface InitiatePaymentResult extends ServiceResult, AuditableResult {

    class Success implements InitiatePaymentResult {
        private final InitiateOffencePaymentResponse response;

        public Success(InitiateOffencePaymentResponse response) {
            this.response = response;
        }

        public InitiateOffencePaymentResponse getResponse() {
            return response;
        }

        @Override
        public Result toAction() {
            Logger.debug("Redirecting to payment form");
            return redirect(controllers.routes.ApplicationController.displayPaymentForm());
        }

        @Override
        public String getPageMovement() {
            return "paymentReceiptToPaymentPage";
        }
    }

    class Error implements InitiatePaymentResult {

        @Override
        public Result toAction() {
            Logger.error("Redirecting to payment error page");
            return redirect(routes.ErrorController.displayPaymentErrorPage());
        }

        @Override
        public String getPageMovement() {
            return "paymentReceiptToPaymentErrorPage";
        }
    }

}
