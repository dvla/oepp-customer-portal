package services;

import com.fasterxml.jackson.core.type.TypeReference;
import configuration.inject.ConfigurationValue;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import services.result.InitiatePaymentResult;
import services.result.TransactionResult;
import uk.gov.dvla.core.error.ErrorResult;
import uk.gov.dvla.error.PaymentServiceErrors;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentRequest;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentResponse;
import utils.JSON;
import utils.URLParser;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletionStage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class PaymentServiceClient {

    private final URL paymentServiceURI;

    @Inject
    private WSClient ws;

    @Inject
    public PaymentServiceClient(@ConfigurationValue(key = "paymentService.baseURL") String baseURL) {
        checkArgument(!isNullOrEmpty(baseURL), "Payment service base URL is required");
        try {
            this.paymentServiceURI = URLParser.parse(baseURL);
        } catch (URLParser.ParsingException ex) {
            throw new IllegalArgumentException("Payment service base URL is invalid", ex);
        }
    }

    public CompletionStage<InitiatePaymentResult> initiatePayment(InitiateOffencePaymentRequest request) {
        String payload = JSON.stringify(request);
        return ws.url(initiatePaymentURL()).setContentType("application/json").post(payload).thenApplyAsync(response -> {
            int responseStatus = response.getStatus();
            String responseBody = response.getBody();
            switch (responseStatus) {
                case Http.Status.OK:
                    Logger.debug("Payment has been initialised with payload {} (status: {}, response body: {})", payload, responseStatus, responseBody);
                    return new InitiatePaymentResult.Success(Json.fromJson(response.asJson(), InitiateOffencePaymentResponse.class));
                default:
                    Logger.error("Payment initialisation failed with payload {} because unexpected response has been received (status: {}, response body: {})", payload, responseStatus, responseBody);
                    return new InitiatePaymentResult.Error();
            }
        }).exceptionally(throwable -> {
            Logger.error("Payment initialisation failed with payload {} because {} exception has been thrown", payload, throwable.getClass().getCanonicalName(), throwable);
            return new InitiatePaymentResult.Error();
        });
    }

    private String initiatePaymentURL() {
        String path = "/payment/initiate";
        try {
            return paymentServiceURI.toURI().resolve(path).toASCIIString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public CompletionStage<TransactionResult> fulfillTransaction(Long paymentID) {
        return ws.url(fulfillTransactionURL(paymentID)).setContentType("application/json").put("").thenApplyAsync(response -> {
            int responseStatus = response.getStatus();
            String responseBody = response.getBody();
            switch (responseStatus) {
                case Http.Status.NO_CONTENT:
                    Logger.debug("Payment {} has been fulfilled (status: {}, response body: {})", paymentID, responseStatus, responseBody);
                    return new TransactionResult.Success();
                case Http.Status.PRECONDITION_FAILED:
                    ErrorResult<PaymentServiceErrors> result = readErrorResult(response);
                    switch (result.getError()) {
                        case PAYMENT_NOT_AUTHORISED:
                            Logger.debug("Payment {} has not been authorised (status: {}, response body: {})", paymentID, responseStatus, responseBody);
                            return new TransactionResult.NotAuthorised();
                    }
                default:
                    Logger.error("Payment {} fulfillment failed because unexpected response has been received (status: {}, response body: {})", paymentID, responseStatus, responseBody);
                    return new TransactionResult.Error();
            }
        }).exceptionally(throwable -> {
            Logger.error("Payment {} fulfillment failed because {} exception has been thrown", paymentID, throwable.getClass().getCanonicalName(), throwable);
            return new TransactionResult.Error();
        });
    }

    private String fulfillTransactionURL(Long paymentID) {
        String path = "/payment/fulfill/" + paymentID;
        try {
            return paymentServiceURI.toURI().resolve(path).toASCIIString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ErrorResult<PaymentServiceErrors> readErrorResult(WSResponse response) {
        try {
            return Json.mapper().readValue(response.asByteArray(), new TypeReference<ErrorResult<PaymentServiceErrors>>() {});
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
