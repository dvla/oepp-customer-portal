package logging;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import play.mvc.Controller;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Module extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.subclassesOf(Controller.class), new AbstractMatcher<Method>() {

            @Override
            public boolean matches(Method method) {
                return Modifier.isPublic(method.getModifiers());
            }
        }, initMDCInterceptor());
    }

    private MDCInterceptor initMDCInterceptor() {
        MDCInterceptor interceptor = new MDCInterceptor();
        requestInjection(interceptor);
        return interceptor;
    }

}
