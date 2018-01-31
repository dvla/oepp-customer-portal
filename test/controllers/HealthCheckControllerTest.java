package controllers;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import metrics.health.checks.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.codahale.metrics.health.HealthCheck.Result.healthy;
import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static play.test.Helpers.contentAsString;

@RunWith(Parameterized.class)
public class HealthCheckControllerTest extends BaseControllerTest {

    @Parameterized.Parameters(name = "{1}")
    public static List<Object[]> data() {
        return Arrays.asList(
                new Object[][]{
                        {
                                ImmutableMap.builder()
                                        .put("securityService.bruteForceCheckingEnabled", false)
                                        .put("audit.enabled", false)
                                        .build(),
                                "fixtures/healthchecks/healthy-core-dependencies.json"
                        },
                        {
                                ImmutableMap.builder()
                                        .put("securityService.bruteForceCheckingEnabled", true)
                                        .put("audit.enabled", false)
                                        .build(),
                                "fixtures/healthchecks/healthy-brute-force-protection-enabled.json"
                        },
                        {
                                ImmutableMap.builder()
                                        .put("securityService.bruteForceCheckingEnabled", false)
                                        .put("audit.enabled", true)
                                        .build(),
                                "fixtures/healthchecks/healthy-auditing-enabled.json"
                        },
                        {
                                ImmutableMap.builder()
                                        .put("securityService.bruteForceCheckingEnabled", true)
                                        .put("audit.enabled", true)
                                        .build(),
                                "fixtures/healthchecks/healthy-all-dependencies.json"
                        }
                }
        );
    }

    @Parameterized.Parameter
    public Map<String, Object> configuration;
    @Parameterized.Parameter(1)
    public String expectedResponse;

    public HealthCheckControllerTest() {
        this.builder = builder.overrides(new AbstractModule() {
            @Override
            protected void configure() {
                bind(SessionDatabaseHealthCheck.class).toInstance(mockHealthCheck(SessionDatabaseHealthCheck.class));
                bind(OffenceServiceHealthCheck.class).toInstance(mockHealthCheck(OffenceServiceHealthCheck.class));
                bind(PaymentServiceHealthCheck.class).toInstance(mockHealthCheck(PaymentServiceHealthCheck.class));
                bind(SecurityServiceHealthCheck.class).toInstance(mockHealthCheck(SecurityServiceHealthCheck.class));
                bind(AuditMessageBrokerHealthCheck.class).toInstance(mockHealthCheck(AuditMessageBrokerHealthCheck.class));
            }
        });
    }

    @Override
    protected play.Application provideApplication() {
        return builder.configure(configuration).build();
    }

    private <T extends HealthCheck> T mockHealthCheck(Class<T> healthCheckClass) {
        T mock = mock(healthCheckClass);
        when(mock.execute()).thenReturn(healthy());
        return mock;
    }

    @Test
    public void checkServiceHealthReturningStatusOnly_shouldReturn200ResponseWithoutResponseBody() throws Exception {
        makeRequest(HTTP.head("/healthcheck"), (result) -> {
            assertThat(result.status(), is(200));
            assertThat(contentAsString(result), isEmptyString());
        });
    }

    @Test
    public void checkServiceHealthReturningReport_shouldReturn200ResponseWithDesiredResponseBody() throws Exception {
        makeRequest(HTTP.get("/healthcheck"), (result) -> {
            assertThat(result.status(), is(200));
            assertThat(contentAsString(result), equalTo(resourceAsString(expectedResponse)));
        });
    }

    private String resourceAsString(String name) {
        try {
            return Resources.toString(Resources.getResource(name), defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}