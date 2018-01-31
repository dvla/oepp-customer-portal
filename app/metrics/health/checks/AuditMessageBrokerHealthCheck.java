package metrics.health.checks;

import configuration.inject.ConfigurationValue;
import metrics.health.checks.base.RabbitMQHealthCheck;

import javax.inject.Inject;

public class AuditMessageBrokerHealthCheck extends RabbitMQHealthCheck {

    @Inject
    public AuditMessageBrokerHealthCheck(@ConfigurationValue(key = "audit.message-broker.host") String host,
                                         @ConfigurationValue(key = "audit.message-broker.port") int port,
                                         @ConfigurationValue(key = "audit.message-broker.username") String username,
                                         @ConfigurationValue(key = "audit.message-broker.password") String password,
                                         @ConfigurationValue(key = "metrics.healthcheck.timeout") int timeout) {
        super(host, port, username, password);
        setTimeout(timeout);
    }

}
