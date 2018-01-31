package services;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import play.Application;
import play.test.Helpers;
import play.test.WithApplication;
import services.result.InitiatePaymentResult;
import services.result.TransactionResult;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import static java.nio.charset.Charset.defaultCharset;
import static org.junit.Assert.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static play.mvc.Http.Status.*;

public class PaymentServiceClientTest extends WithApplication {

    private static final Long PAYMENT_ID = 1L;

    private static final String INITIATE_PAYMENT_ENDPOINT = "/payment/initiate";
    private static final String FULFILL_TRANSACTION_ENDPOINT = "/payment/fulfill/1";

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private MockServerClient server;
    private PaymentServiceClient client;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ImmutableMap.<String, String>builder()
                .put("paymentService.baseURL", "http://localhost:" + mockServerRule.getPort())
                .build()
        );
    }

    @Before
    public void init() {
        server = new MockServerClient("localhost", mockServerRule.getPort());
        client = app.injector().instanceOf(PaymentServiceClient.class);
    }

    @Test
    public void initiatePayment_shouldReturnErrorWhenPaymentIsInitialised() throws IOException, ExecutionException, InterruptedException {
        mockInitiatePaymentRequest(OK, resourceAsString("fixtures/initiate-payment-response.json").getBytes());

        InitiatePaymentResult result = client.initiatePayment(initiateOffencePaymentRequest()).toCompletableFuture().get();

        verifyInitiatePaymentRequest();
        assertThat(result, Matchers.instanceOf(InitiatePaymentResult.Success.class));
    }

    @Test
    public void initiatePayment_shouldReturnErrorWhenInitialisationFailed() throws IOException, ExecutionException, InterruptedException {
        mockInitiatePaymentRequest(INTERNAL_SERVER_ERROR, resourceAsString("fixtures/initiate-payment-error-response.json").getBytes());

        InitiatePaymentResult result = client.initiatePayment(initiateOffencePaymentRequest()).toCompletableFuture().get();

        verifyInitiatePaymentRequest();
        assertThat(result, Matchers.instanceOf(InitiatePaymentResult.Error.class));
    }

    @Test
    public void fulfillTransaction_shouldReturnSuccessWhenPaymentIsFulfilled() throws IOException, ExecutionException, InterruptedException {
        mockFulfillTransactionRequest(NO_CONTENT, null);

        TransactionResult result = client.fulfillTransaction(PAYMENT_ID).toCompletableFuture().get();

        verifyFulfillTransactionRequest();
        assertThat(result, Matchers.instanceOf(TransactionResult.Success.class));
    }

    @Test
    public void fulfillTransaction_shouldReturnNotAuthorisedWhenPaymentIsNotAuthorised() throws IOException, ExecutionException, InterruptedException {
        mockFulfillTransactionRequest(PRECONDITION_FAILED, resourceAsString("fixtures/fulfill-transaction-not-authorised-response.json").getBytes());

        TransactionResult result = client.fulfillTransaction(PAYMENT_ID).toCompletableFuture().get();

        verifyFulfillTransactionRequest();
        assertThat(result, Matchers.instanceOf(TransactionResult.NotAuthorised.class));
    }

    @Test
    public void fulfillTransaction_shouldReturnErrorForWhenPaymentFailed() throws IOException, ExecutionException, InterruptedException {
        mockFulfillTransactionRequest(INTERNAL_SERVER_ERROR, resourceAsString("fixtures/fulfill-transaction-error-response.json").getBytes());

        TransactionResult result = client.fulfillTransaction(PAYMENT_ID).toCompletableFuture().get();

        verifyFulfillTransactionRequest();
        assertThat(result, Matchers.instanceOf(TransactionResult.Error.class));
    }

    private InitiateOffencePaymentRequest initiateOffencePaymentRequest() throws MalformedURLException {
        return new InitiateOffencePaymentRequest.Builder()
                .setTransactionID("OEPP-123456789")
                .setPaymentAmount(BigDecimal.valueOf(60.00))
                .setLanguage("EN")
                .setPostAuthorizeCallbackURL(new URL("http://localhost/payment/finish"))
                .create();
    }

    private void mockInitiatePaymentRequest(int responseStatusCode, byte[] responseBody) {
        HttpResponse response = response().withStatusCode(responseStatusCode);

        if (responseBody != null) {
            response.withBody(responseBody);
        }

        server.when(initiatePaymentRequest()).respond(response);
    }

    private void verifyInitiatePaymentRequest() {
        server.verify(initiatePaymentRequest());
    }

    private HttpRequest initiatePaymentRequest() {
        return request()
                .withPath(INITIATE_PAYMENT_ENDPOINT)
                .withMethod("POST")
                .withHeader("Content-Type", "application/json");
    }

    private void mockFulfillTransactionRequest(int responseStatusCode, byte[] responseBody) {
        HttpResponse response = response().withStatusCode(responseStatusCode);

        if (responseBody != null) {
            response.withBody(responseBody);
        }

        server.when(fulfillTransactionRequest()).respond(response);
    }

    private void verifyFulfillTransactionRequest() throws IOException {
        server.verify(fulfillTransactionRequest());
    }

    private HttpRequest fulfillTransactionRequest() {
        return request()
                .withPath(FULFILL_TRANSACTION_ENDPOINT)
                .withMethod("PUT")
                .withHeader("Content-Type", "application/json");
    }

    private String resourceAsString(String name) throws IOException {
        return Resources.toString(Resources.getResource(name), defaultCharset());
    }
}
