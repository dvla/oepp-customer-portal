package metrics.health.reporting;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class HealthCheckController extends Controller {

    private final HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    // HEAD
    public Result checkServiceHealthReturningStatusOnly() {
        return checkServiceHealth(false);
    }

    // GET
    public Result checkServiceHealthReturningReport() {
        return checkServiceHealth(true);
    }

    private Result checkServiceHealth(boolean printReport) {
        Map<String, HealthCheck.Result> results = runHealthChecks();

        if (results.isEmpty()) {
            return status(Http.Status.NOT_IMPLEMENTED);
        }

        if (printReport) {
            return status(httpStatus(results), new HealthCheckReport(results));
        } else {
            return status(httpStatus(results));
        }
    }

    private Map<String, HealthCheck.Result> runHealthChecks() {
        ExecutorService executor = null;
        try {
            executor = newFixedThreadPool(healthCheckRegistry.getNames().size());
            return healthCheckRegistry.runHealthChecks(executor);
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }

    private int httpStatus(Map<String, HealthCheck.Result> results) {
        if (isAllHealthy(results)) {
            return Http.Status.OK;
        } else {
            return Http.Status.INTERNAL_SERVER_ERROR;
        }
    }

    private boolean isAllHealthy(Map<String, HealthCheck.Result> results) {
        return results.values().stream().allMatch(HealthCheck.Result::isHealthy);
    }

    protected void register(String name, HealthCheck healthCheck) {
        healthCheckRegistry.register(name, healthCheck);
    }

    protected SortedSet<String> getNames() {
        return healthCheckRegistry.getNames();
    }
}
