package framework.filters;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.route;

@RunWith(Enclosed.class)
public class HttpsSecurityHeadersFilterTest {

    public static class EnabledTest extends WithApplication {

        @Override
        protected Application provideApplication() {
            return new GuiceApplicationBuilder().configure("play.filters.headers.strictTransportSecurity", "max-age=7776000; includeSubDomains; preloaded").build();
        }

        @Test
        public void mustSendStrictTransportSecurityHeaderWhenHeaderValueIsConfigured() {
            Result result = makeFakeRequest();

            assertThat(result.headers().containsKey(HttpsSecurityHeadersFilter.HEADER_NAME), is(true));
            assertThat(result.header(HttpsSecurityHeadersFilter.HEADER_NAME).get(), is("max-age=7776000; includeSubDomains; preloaded"));
        }

    }

    public static class DisabledTest extends WithApplication {

        @Override
        protected Application provideApplication() {
            Map<String, Object> configuration = Maps.newHashMap();
            configuration.put("play.filters.headers.strictTransportSecurity", null);
            return new GuiceApplicationBuilder().configure(configuration).build();
        }

        @Test
        public void mustNotSendHttpStrictTransportSecurityHeaderWhenHeaderValueIsNotConfigured() {
            Result result = makeFakeRequest();

            assertThat(result.headers().containsKey(HttpsSecurityHeadersFilter.HEADER_NAME), is(false));
        }

    }

    private static Result makeFakeRequest() {
        return route(Helpers.fakeRequest("GET", "/"));
    }

}
