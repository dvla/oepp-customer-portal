package metrics.health.checks;

import com.google.inject.ProvisionException;
import metrics.health.checks.base.RedisHealthCheck;
import org.redisson.RedissonClient;

import javax.inject.Inject;
import javax.inject.Provider;

public class SessionDatabaseHealthCheck extends RedisHealthCheck {

    @Inject
    public SessionDatabaseHealthCheck(Provider<RedissonClient> redisClientProvider) {
        super(() -> {
            try {
                RedissonClient redisClient = redisClientProvider.get();
                return redisClient.getNodesGroup().pingAll();
            } catch (ProvisionException ex) {
                throw ex.getCause(); // rethrowing exception cause to hide unnecessary provisioning information
            }
        });
    }
}
