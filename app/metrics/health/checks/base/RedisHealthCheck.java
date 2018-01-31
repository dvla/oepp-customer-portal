package metrics.health.checks.base;

import com.codahale.metrics.health.HealthCheck;

public class RedisHealthCheck extends HealthCheck {

    private final Client client;

    public RedisHealthCheck(Client client) {
        this.client = client;
    }

    @Override
    protected Result check() {
        try {
            return client.ping() ? Result.healthy() : Result.unhealthy("At least one node didn't respond to PING command");
        } catch (Throwable throwable) {
            return Result.unhealthy(throwable);
        }
    }

    public interface Client {

        boolean ping() throws Throwable;

    }

}
