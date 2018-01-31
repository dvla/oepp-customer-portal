package session.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import session.SessionManager;

import javax.inject.Inject;

public class DestroySessionAfterwardsInterceptor implements MethodInterceptor {

    @Inject
    private SessionManager sessionManager;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object processingResult = invocation.proceed();
        sessionManager.destroySession();
        return processingResult;
    }

}
