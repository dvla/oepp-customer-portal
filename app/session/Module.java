package session;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import session.aop.DestroySessionAfterwards;
import session.aop.DestroySessionAfterwardsInterceptor;
import session.aop.RequireSession;
import session.aop.RequireSessionInterceptor;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(RequireSession.class), initRequireSessionInterceptor());
        bindInterceptor(Matchers.any(), Matchers.annotatedWith(DestroySessionAfterwards.class), initDestroySessionAfterwardsInterceptor());
    }

    private RequireSessionInterceptor initRequireSessionInterceptor() {
        RequireSessionInterceptor interceptor = new RequireSessionInterceptor();
        requestInjection(interceptor);
        return interceptor;
    }

    private DestroySessionAfterwardsInterceptor initDestroySessionAfterwardsInterceptor() {
        DestroySessionAfterwardsInterceptor interceptor = new DestroySessionAfterwardsInterceptor();
        requestInjection(interceptor);
        return interceptor;
    }

}
