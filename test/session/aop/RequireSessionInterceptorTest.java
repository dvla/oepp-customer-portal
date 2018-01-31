package session.aop;

import exceptions.IllegalSessionStateException;
import models.Payment;
import models.PaymentReceipt;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import session.SessionManager;
import session.aop.RequireSessionInterceptor;
import uk.gov.dvla.domain.Offence;

import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.URLParser.parse;

public class RequireSessionInterceptorTest {

    private SessionManager.Session session;
    private RequireSessionInterceptor.Preconditions preconditions;

    @Before
    public void init() {
        session = mock(SessionManager.Session.class);
        preconditions = new RequireSessionInterceptor().new Preconditions(session);
    }

    @Test(expected = IllegalSessionStateException.class)
    public void checkOffenceExists_mustThrowExceptionWhenOffenceDoesNotExistInSession() {
        when(session.offence()).thenReturn(Optional.empty());
        preconditions.checkOffenceExists();
    }

    @Test
    public void checkOffenceExists_mustNotThrowExceptionWhenOffenceExistsInSession() {
        when(session.offence()).thenReturn(Optional.of(new Offence.Builder().create()));
        preconditions.checkOffenceExists();
    }

    @Test(expected = IllegalSessionStateException.class)
    public void checkLockedDateExists_mustThrowExceptionWhenLockedDateDoesNotExistInSession() {
        when(session.lockedDate()).thenReturn(Optional.empty());
        preconditions.checkLockedDateExists();
    }

    @Test
    public void checkLockedDateExists_mustNotThrowExceptionWhenLockedDateExistsInSession() {
        when(session.lockedDate()).thenReturn(Optional.of(DateTime.now().toDate()));
    preconditions.checkLockedDateExists();
    }

    @Test(expected = IllegalSessionStateException.class)
    public void checkPaymentReceiptExists_mustThrowExceptionWhenPaymentReceiptDoesNotExistInSession() {
        when(session.paymentReceipt()).thenReturn(Optional.empty());
        preconditions.checkPaymentReceiptExists();
    }

    @Test
    public void checkPaymentReceiptExists_mustNotThrowExceptionWhenPaymentReceiptExistsInSession() {
        when(session.paymentReceipt()).thenReturn(Optional.of(new PaymentReceipt(false, null)));
        preconditions.checkPaymentReceiptExists();
    }

    @Test(expected = IllegalSessionStateException.class)
    public void checkPaymentExists_mustThrowExceptionWhenPaymentDoesNotExistInSession() {
        when(session.payment()).thenReturn(Optional.empty());
        preconditions.checkPaymentExists();
    }

    @Test
    public void checkPaymentExists_mustNotThrowExceptionWhenPaymentExistsInSession() {
        when(session.payment()).thenReturn(Optional.of(payment()));
        preconditions.checkPaymentExists();
    }

    @Test(expected = IllegalSessionStateException.class)
    public void checkPaymentDateExists_mustThrowExceptionWhenPaymentDateDoesNotExistInSession() {
        when(session.payment()).thenReturn(Optional.of(payment().setPaidDate(Optional.<Date>empty())));
        preconditions.checkPaymentDateExists();
    }

    @Test
    public void checkPaymentDateExists_mustNotThrowExceptionWhenPaymentDateExistsInSession() {
        when(session.payment()).thenReturn(Optional.of(payment().setPaidDate(Optional.of(new Date()))));
        preconditions.checkPaymentDateExists();
    }

    private Payment payment() {
        return new Payment(1L, parse("http://localhost"));
    }

}