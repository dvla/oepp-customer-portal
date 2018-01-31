package metrics.health.reporting;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import play.mvc.Result;

import java.io.IOException;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static com.codahale.metrics.health.HealthCheck.Result.unhealthy;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.contentAsString;

@RunWith(MockitoJUnitRunner.class)
public class HealthCheckControllerTest {

    private HealthCheckController controller;

    @Before
    public void init() {
        controller = new HealthCheckController();
    }

    @Test
    public void checkServiceHealthReturningStatusOnly_shouldReturn200ResponseWhenAllHealthChecksAreHealthy() throws Exception {
        controller.register("database", healthCheckReturning(healthy()));
        controller.register("service", healthCheckReturning(healthy()));

        Result result = controller.checkServiceHealthReturningStatusOnly();

        assertThat(result.status(), is(200));
        assertThat(contentAsString(result), isEmptyString());
    }

    @Test
    public void checkServiceHealthReturningReport_shouldReturn200ResponseWhenAllHealthChecksAreHealthy() throws Exception {
        controller.register("database", healthCheckReturning(healthy()));
        controller.register("service", healthCheckReturning(healthy()));

        Result result = controller.checkServiceHealthReturningReport();

        assertThat(result.status(), is(200));
        assertThat(contentAsString(result), equalTo(resourceAsString("fixtures/healthchecks/healthy.json")));
    }

    @Test
    public void checkServiceHealthReturningStatusOnly_shouldReturn500ResponseWhenAtLeastOneHealthChecksAreUnhealthy() throws Exception {
        controller.register("database", healthCheckReturning(healthy()));
        controller.register("service", healthCheckReturning(unhealthy("Something went wrong")));

        Result result = controller.checkServiceHealthReturningStatusOnly();

        assertThat(result.status(), is(500));
        assertThat(contentAsString(result), isEmptyString());
    }

    @Test
    public void checkServiceHealthReturningReport_shouldReturn500ResponseWhenAtLeastOneHealthChecksAreUnhealthy() throws Exception {
        controller.register("database", healthCheckReturning(healthy()));
        controller.register("service", healthCheckReturning(unhealthy("Something went wrong")));

        Result result = controller.checkServiceHealthReturningReport();

        assertThat(result.status(), is(500));
        assertThat(contentAsString(result), equalTo(resourceAsString("fixtures/healthchecks/unhealthy.json")));
    }

    @Test(timeout = 1250)
    public void checkServiceHealthReturningStatusOnly_shouldRunChecksInParallel() throws Exception {
        controller.register("database", healthCheckReturningWithDelay(healthy(), 1000));
        controller.register("service", healthCheckReturningWithDelay(healthy(), 1000));
        controller.register("cache", healthCheckReturningWithDelay(healthy(), 1000));

        assertThat(controller.checkServiceHealthReturningStatusOnly().status(), is(200));
    }

    @Test(timeout = 1250)
    public void checkServiceHealthReturningReport_shouldRunChecksInParallel() throws Exception {
        controller.register("database", healthCheckReturningWithDelay(healthy(), 1000));
        controller.register("service", healthCheckReturningWithDelay(healthy(), 1000));
        controller.register("cache", healthCheckReturningWithDelay(healthy(), 1000));

        assertThat(controller.checkServiceHealthReturningReport().status(), is(200));
    }

    private HealthCheck healthCheckReturning(final HealthCheck.Result result) {
        return healthCheckReturningWithDelay(result, 0); // no delay
    }

    private HealthCheck healthCheckReturningWithDelay(final HealthCheck.Result result, int delayInMilliseconds) {
        return new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                Thread.sleep(delayInMilliseconds);
                return result;
            }
        };
    }

    private String resourceAsString(String name) {
        try {
            return Resources.toString(Resources.getResource(name), defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}