package metrics.health.checks;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import metrics.health.checks.base.AbstractServiceHealthCheck;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.junit.MockServerRule;
import play.Application;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;
import static com.google.common.collect.Lists.newArrayList;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@RunWith(Parameterized.class)
public class ServiceHealthChecksTest extends WithApplication {

    @Parameterized.Parameters(name = "Health check class: {0}")
    public static Iterable<Class<?>> data() {
        return newArrayList(OffenceServiceHealthCheck.class, PaymentServiceHealthCheck.class, SecurityServiceHealthCheck.class);
    }

    @Parameterized.Parameter
    public Class<AbstractServiceHealthCheck> healthCheckClass;

    @Rule
    public MockServerRule mockServerRule = new MockServerRule(this);

    private MockServerClient server;
    private AbstractServiceHealthCheck healthCheck;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ImmutableMap.<String, String>builder()
                .put("offenceService.adminURL", "http://localhost:" + mockServerRule.getPort())
                .put("paymentService.adminURL", "http://localhost:" + mockServerRule.getPort())
                .put("securityService.adminURL", "http://localhost:" + mockServerRule.getPort())
                .build()
        );
    }

    @Before
    public void init() {
        server = new MockServerClient("localhost", mockServerRule.getPort());
        healthCheck = app.injector().instanceOf(healthCheckClass);
    }

    @Test
    public void check_shouldReturnHealthyWhenUnderlyingServiceReturned200Response() throws Exception {
        String serverResponseBody = resourceAsString("fixtures/healthchecks/healthy.json");

        server.when(request()).respond(response().withStatusCode(200).withHeader("Content-Type", "application/json").withBody(serverResponseBody));

        HealthCheck.Result result = healthCheck.execute();
        assertThat(result, is(healthy()));
    }

    @Test
    public void check_shouldReturnUnhealthyWhenUnderlyingServiceReturnedNon200Response() throws Exception {
        String serverResponseBody = resourceAsString("fixtures/healthchecks/unhealthy.json");

        server.when(request()).respond(response().withStatusCode(500).withHeader("Content-Type", "application/json").withBody(serverResponseBody));

        HealthCheck.Result result = healthCheck.execute();
        assertThat(result, is(unhealthy("Health check returned: " + serverResponseBody.replace("\"", "'"))));
    }

    @Test
    public void check_shouldReturnUnhealthyWhenFailedToConnectToHealthCheckEndpoint() throws Exception {
        server.stop();

        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy(), is(false));
        assertThat(result.getMessage(), startsWith("java.net.ConnectException: Connection refused: localhost"));
    }

    private String resourceAsString(String name) {
        try {
            return Resources.toString(Resources.getResource(name), defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}