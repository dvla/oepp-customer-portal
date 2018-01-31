package controllers;


import exceptions.IllegalSessionStateException;
import models.Payment;
import models.PaymentReceipt;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.test.WithApplication;
import session.SessionManager;
import uk.gov.dvla.domain.Offence;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static play.test.Helpers.fakeRequest;
import static utils.URLParser.parse;

public class ApplicationStateValidationTest extends WithApplication {

    private ApplicationController controller;
    private SessionManager.Session session;

    private final ExceptionExpectation MISSING_CASE_EXCEPTION = new ExceptionExpectation(IllegalSessionStateException.class, "Missing offence in the session");
    private final ExceptionExpectation MISSING_PAYMENT_EXCEPTION = new ExceptionExpectation(IllegalSessionStateException.class, "Missing payment in the session");
    private final ExceptionExpectation MISSING_PAYMENT_DATE_EXCEPTION = new ExceptionExpectation(IllegalSessionStateException.class, "Missing paid date in the session");
    private final ExceptionExpectation MISSING_PAYMENT_RECEIPT_EXCEPTION = new ExceptionExpectation(IllegalSessionStateException.class, "Missing email decision in the session");

    @Before
    public void init() throws Exception {
        Http.Context.current.set(new Http.Context(fakeRequest().session("id", "my-session-id")));

        controller = app.injector().instanceOf(ApplicationController.class);
        session = app.injector().instanceOf(SessionManager.class).session();
        session.clear();
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingPenaltySummaryPageAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.displayPenaltySummaryPage());
    }

    @Test
    public void shouldThrowExceptionWhenPenaltyAcceptedAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.onPenaltyAcceptance());
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingReceiptSelectionFormAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.displayReceiptSelectionForm());
    }

    @Test
    public void shouldThrowExceptionWhenReceiptSelectedAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.onReceiptSelection());
    }

    @Test
    public void shouldThrowExceptionWhenInitiatingPaymentAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.initPayment());
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingPaymentFormAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.displayPaymentForm());
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingPaymentFormAndPaymentDoesNotExistInSession() throws Throwable {
        expectException(MISSING_PAYMENT_EXCEPTION, () -> {
            session.setOffence(offence());
            controller.displayPaymentForm();
        });
    }

    @Test
    public void shouldThrowExceptionWhenFulfillingTransactionAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.fulfillTransaction());
    }

    @Test
    public void shouldThrowExceptionWhenFulfillingTransactionAndPaymentDoesNotExistInSession() throws Throwable {
        expectException(MISSING_PAYMENT_EXCEPTION, () -> {
            session.setOffence(offence());
            controller.fulfillTransaction();
        });
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingPaymentConfirmationAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.displayPaymentConfirmation());
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingPaymentConfirmationAndPaymentDoesNotExistInSession() throws Throwable {
        expectException(MISSING_PAYMENT_EXCEPTION, () -> {
            session.setOffence(offence());
            session.setPaymentReceipt(PaymentReceipt.receiptNotRequested());
            controller.displayPaymentConfirmation();
        });
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingPaymentConfirmationAndPaymentReceiptDoesNotExistInSession() throws Throwable {
        expectException(MISSING_PAYMENT_RECEIPT_EXCEPTION, () -> {
            session.setOffence(offence());
            session.setPayment(payment().setPaidDate(Optional.empty()));
            controller.displayPaymentConfirmation();
        });
    }

    @Test
    public void shouldThrowExceptionWhenDisplayingPaymentConfirmationAndPaymentDateDoesNotExistInSession() throws Throwable {
        expectException(MISSING_PAYMENT_DATE_EXCEPTION, () -> {
            session.setOffence(offence());
            session.setPaymentReceipt(PaymentReceipt.receiptNotRequested());
            session.setPayment(payment().setPaidDate(Optional.empty()));
            controller.displayPaymentConfirmation();
        });
    }

    @Test
    public void shouldThrowExceptionWhenWhatNextSelectedAndOffenceDoesNotExistInSession() throws Throwable {
        expectException(MISSING_CASE_EXCEPTION, () -> controller.onWhatNextSelection());
    }

    private void expectException(ExceptionExpectation expectedException, Runnable runnable) {
        try {
            runnable.run();
            fail();
        } catch (Exception ex) {
            assertEquals(expectedException.type, ex.getClass());
            assertEquals(expectedException.message, ex.getMessage());
        }
    }

    private class ExceptionExpectation {

        private final Class<? extends Exception> type;
        private final String message;

        private ExceptionExpectation(Class<? extends Exception> type, String message) {
            this.type = type;
            this.message = message;
        }
    }

    private Offence offence() {
        return new Offence.Builder().setCriteria(new Offence.Criteria(1L, "CV02AAA")).create();
    }

    private Payment payment() {
        return new Payment(1L, parse("http://localhost"));
    }

}
