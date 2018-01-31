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
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SessionDatabaseHealthCheckIT extends WithApplication {

    @Rule
    public final ProxyServerRule proxyServerRule = new ProxyServerRule("localhost", 6379);

    @Inject
    private SessionDatabaseHealthCheck healthCheck;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ImmutableMap.of("redisDatabase.address", "localhost:" + proxyServerRule.getPort()));
    }

    @Before
    public void init() {
        healthCheck = app.injector().instanceOf(SessionDatabaseHealthCheck.class);
    }

    @Test
    public void check_shouldReturnHealthyWhenDatabaseIsRunning() {
        HealthCheck.Result result = healthCheck.execute();
        assertThat(result, is(healthy()));
    }

    @Test
    public void check_shouldReturnUnhealthyWhenDatabaseIsDown() {
        proxyServerRule.stopServer();

        HealthCheck.Result result = healthCheck.execute();
        assertThat(result.isHealthy(), is(false));
        assertThat(result.getMessage(), is("Can't init enough connections amount! from localhost/127.0.0.1:" + proxyServerRule.getPort()));
    }

}