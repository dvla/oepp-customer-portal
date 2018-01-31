package session.aop;

import exceptions.IllegalSessionStateException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import session.SessionManager;

import javax.inject.Inject;

public class RequireSessionInterceptor implements MethodInterceptor {

    @Inject
    private SessionManager sessionManager;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        SessionManager.Session session = sessionManager.existingSession().orElseThrow(() -> new IllegalSessionStateException("Session is required"));

        validateSession(session, invocation.getMethod().getAnnotation(RequireSession.class));

        return invocation.proceed();
    }

    private void validateSession(SessionManager.Session session, RequireSession annotation) {
        Preconditions preconditions = new Preconditions(session);
        if (annotation.withOffence()) {
            preconditions.checkOffenceExists();
        }
        if (annotation.withPayment()) {
            preconditions.checkPaymentExists();
        }
        if (annotation.withPaymentReceipt()) {
            preconditions.checkPaymentReceiptExists();
        }
        if (annotation.withPaymentDate()) {
            preconditions.checkPaymentDateExists();
        }
        if (annotation.withLockedDate()) {
            preconditions.checkLockedDateExists();
        }
    }

    class Preconditions {

        private SessionManager.Session session;

        Preconditions(SessionManager.Session session) {
            this.session = session;
        }

        void checkOffenceExists() {
            if (!session.offence().isPresent()) {
                throw new IllegalSessionStateException("Missing offence in the session");
            }
        }

        void checkPaymentExists() {
            if (!session.payment().isPresent()) {
                throw new IllegalSessionStateException("Missing payment in the session");
            }
        }

        void checkPaymentReceiptExists() {
            if (!session.paymentReceipt().isPresent()) {
                throw new IllegalSessionStateException("Missing email decision in the session");
            }
        }

        void checkPaymentDateExists() {
            checkPaymentExists();
            if (!session.payment().get().getPaidDate().isPresent()) {
                throw new IllegalSessionStateException("Missing paid date in the session");
            }
        }

        void checkLockedDateExists() {
            if (!session.lockedDate().isPresent()) {
                throw new IllegalSessionStateException("Missing locked date in the session");
            }
        }
    }

}
