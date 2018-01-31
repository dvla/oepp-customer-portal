package controllers;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import play.inject.guice.GuiceApplicationBuilder;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class ApplicationControllerRedirectStartPageTest {

    public static class RelativeRedirectTest extends BaseControllerTest {
        private String startPageURI = "/before-you-start";
        public RelativeRedirectTest() {
            this.builder = makeBuilder(startPageURI);
        }

        @Test
        public void shouldRedirectToConfiguredPath() {
            makeRequest(HTTP.get("/"), (result) -> {
                assertThat(result.status(), is(303));
                assertThat(result.redirectLocation().get(), is(startPageURI));
            });
        }
    }

    public static class AbsoluteRedirectTest extends BaseControllerTest {
        private String startPageURI = "https://gov.uk/start";

        public AbsoluteRedirectTest() {
            this.builder = makeBuilder(startPageURI);
        }

        @Test
        public void shouldRedirectToConfiguredPath() {
            makeRequest(HTTP.get("/"), (result) -> {
                assertThat(result.status(), is(303));
                assertThat(result.redirectLocation().get(), is(startPageURI));
            });
        }
    }

    private static GuiceApplicationBuilder makeBuilder(String startPageURI) {
        return new GuiceApplicationBuilder().configure(ImmutableMap.<String, Object>builder()
                .put("startPageURI", startPageURI)
                .build()
        );
    }
}