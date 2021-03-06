package metrics.health.checks;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import play.Application;
import play.test.Helpers;
import play.test.WithApplication;
import utils.ProxyServerRule;

import javax.inject.Inject;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AuditMessageBrokerHealthCheckIT extends WithApplication {

    @Rule
    public final ProxyServerRule proxyServerRule = new ProxyServerRule("localhost", 5672);

    @Inject
    private AuditMessageBrokerHealthCheck healthCheck;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ImmutableMap.of("audit.message-broker.port", proxyServerRule.getPort()));
    }

    @Before
    public void init() {
        healthCheck = app.injector().instanceOf(AuditMessageBrokerHealthCheck.class);
    }

    @Test
    public void check_shouldReturnHealthyWhenMessageBrokerIsRunning() {
        HealthCheck.Result result = healthCheck.execute();
        assertThat(result, is(healthy()));
    }

    @Test
    public void check_shouldReturnUnhealthyWhenMessageBrokerIsDown() {
        proxyServerRule.stopServer();

        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy(), is(false));
        assertThat(result.getMessage(), containsString("Connection refused"));
    }

}