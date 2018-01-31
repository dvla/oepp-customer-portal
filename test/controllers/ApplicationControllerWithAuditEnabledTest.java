package controllers;

import auditing.AuditService;
import auditing.messages.*;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import models.Payment;
import models.PaymentReceipt;
import org.hamcrest.CustomTypeSafeMatcher;
import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.internal.matchers.InstanceOf;
import services.OffenceServiceClient;
import services.PaymentServiceClient;
import services.email.EmailServiceClient;
import services.result.GetCaseResult;
import services.result.InitiatePaymentResult;
import services.result.TransactionResult;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentResponse;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static controllers.constants.OffenceConstants.*;
import static controllers.constants.PaymentConstants.*;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ApplicationControllerWithAuditEnabledTest extends BaseStatefulControllerTest {

    private final AuditService auditService;
    private final OffenceServiceClient offenceServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    public ApplicationControllerWithAuditEnabledTest() {
        auditService = mock(AuditService.class);
        offenceServiceClient = mock(OffenceServiceClient.class);
        paymentServiceClient = mock(PaymentServiceClient.class);

        builder = builder.configure(ImmutableMap.<String, Object>builder()
                .put("audit.enabled", true)
                .build()
        ).overrides(new AbstractModule() {
            @Override
            protected void configure() {
                bind(AuditService.class).toInstance(auditService);
                bind(EmailServiceClient.class).toInstance(mock(EmailServiceClient.class));
                bind(OffenceServiceClient.class).toInstance(offenceServiceClient);
                bind(PaymentServiceClient.class).toInstance(paymentServiceClient);
            }
        });
    }

    @Test
    public void searchCase_OffenceRecordFoundAudit() throws ExecutionException, InterruptedException {
        verifyCaseRetrievalAudited(OffenceRecordFound.class, new GetCaseResult.Found(offence()));
    }

    @Test
    public void searchCase_OffenceRecordErrorAudit() throws ExecutionException, InterruptedException {
        verifyCaseRetrievalAudited(OffenceRecordError.class, new GetCaseResult.Error());
    }

    @Test
    public void searchCase_OffenceRecordErrorNotFound() throws ExecutionException, InterruptedException {
        verifyCaseRetrievalAudited(OffenceRecordError.class, new GetCaseResult.NotFound());
    }

    @Test
    public void searchCase_OffenceRecordErrorNotSupportedCaseType() throws ExecutionException, InterruptedException {
        verifyCaseRetrievalAudited(OffenceRecordError.class, new GetCaseResult.NotSupportedCaseType());
    }

    @Test
    public void searchCase_OffenceRecordErrorVRMMismatch() throws ExecutionException, InterruptedException {
        verifyCaseRetrievalAudited(OffenceRecordError.class, new GetCaseResult.VehicleRegistrationMarkMismatch());
    }

    @Test
    public void onPenaltyAcceptance_PenaltyAccepted() {
        when(session.offence()).thenReturn(Optional.of(offence()));

        Request request = HTTP.post("/penalty").withFormParameter("untaxedVehicleAcknowledged", "true");
        makeRequest(request, (result) -> verify(auditService, times(1)).sendAuditMessage(argThat(suppliesA(PenaltyAccepted.class))));
    }

    @Test
    public void onReceiptSelection_PaymentInitiated() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.payment()).thenReturn(Optional.of(payment()));
        when(session.paymentReceipt()).thenReturn(Optional.of(new PaymentReceipt(false, "")));
        InitiateOffencePaymentResponse response = new InitiateOffencePaymentResponse.Builder().setPaymentID(PAYMENT_ID).setPaymentReference(PAYMENT_REFERENCE).setPaymentPageUrl(PAYMENT_PAGE_URL).create();

        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Success(response)));

        Request request = HTTP.post("/receipt")
                .withFormParameter("emailReceiptDecision", "false")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> {
            assertThat(result.redirectLocation().get(), is(routes.ApplicationController.displayPaymentForm().url()));
            verify(auditService, times(1)).sendAuditMessage(argThat(suppliesA(PaymentInitiated.class)));
        });
    }

    @Test
    public void onReceiptSelection_PaymentInitiationError() throws ExecutionException, InterruptedException {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.payment()).thenReturn(Optional.of(payment()));
        when(session.paymentReceipt()).thenReturn(Optional.of(new PaymentReceipt(false, "")));

        when(paymentServiceClient.initiatePayment(any())).thenReturn(completedFuture(new InitiatePaymentResult.Error()));

        Request request = HTTP.post("/receipt")
                .withFormParameter("emailReceiptDecision", "false")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> verify(auditService, times(1)).sendAuditMessage(argThat(suppliesA(PaymentInitiationError.class))));
    }

    @Test
    public void displayPaymentConfirmation_TransactionSuccessful() throws ExecutionException, InterruptedException {
        verifyTransactionAudited(TransactionSuccessful.class, new TransactionResult.Success());
    }

    @Test
    public void displayPaymentError_TransactionError() throws ExecutionException, InterruptedException {
        verifyTransactionAudited(TransactionError.class, new TransactionResult.Error());
    }

    @Test
    public void displayPaymentError_TransactionErrorNotAuthorised() throws ExecutionException, InterruptedException {
        verifyTransactionAudited(TransactionError.class, new TransactionResult.NotAuthorised());
    }

    private <T extends AbstractAuditMessage> void verifyTransactionAudited(Class<T> auditMessageClass, TransactionResult transactionResult) throws ExecutionException, InterruptedException {
        Payment payment = payment();
        payment.setPaidDate(Optional.of(DateTime.now().toDate()));
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.payment()).thenReturn(Optional.of(payment));
        when(session.paymentReceipt()).thenReturn(Optional.of(new PaymentReceipt(true, "test@test.com")));
        when(paymentServiceClient.fulfillTransaction(any())).thenReturn(completedFuture(transactionResult));

        Request request = HTTP.post("/payment/finish?" + "session=" + SESSION_ID + "&token=secret")
                .withCSRFToken("secret");

        makeRequest(request, (result) -> verify(auditService, times(1)).sendAuditMessage(argThat(suppliesA(auditMessageClass))));
    }

    private <T extends AbstractAuditMessage> void verifyCaseRetrievalAudited(Class<T> auditMessageClass, GetCaseResult getCaseResult) throws ExecutionException, InterruptedException {
        when(offenceServiceClient.getCase(CASE_NUMBER, VEHICLE_REGISTRATION_MARK, SESSION_ID)).thenReturn(completedFuture(getCaseResult));
        when(session.offence()).thenReturn(Optional.of(offence()));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> verify(auditService, times(1)).sendAuditMessage(argThat(suppliesA(auditMessageClass))));
    }

    private static class SupplierMatcher<T extends AbstractAuditMessage> extends CustomTypeSafeMatcher<Supplier<T>> {

        private final Class<T> expectedType;

        private SupplierMatcher(Class<T> expectedType) {
            super("Wrong supplier type");
            this.expectedType = expectedType;
        }

        @Override
        protected boolean matchesSafely(Supplier<T> supplier) {
            return new InstanceOf(expectedType).matches(supplier.get());
        }
    }

    private static <T extends AbstractAuditMessage> SupplierMatcher<T> suppliesA(Class<T> expectedType) {
        return new SupplierMatcher<T>(expectedType);
    }

}
