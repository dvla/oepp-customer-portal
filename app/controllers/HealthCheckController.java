package controllers;

import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import configuration.inject.ConfigurationValue;
import metrics.health.checks.*;

import javax.inject.Inject;
import javax.inject.Provider;

public class HealthCheckController extends metrics.health.reporting.HealthCheckController {

    @Inject
    public HealthCheckController(SessionDatabaseHealthCheck sessionDatabaseHealthCheck,
                                 OffenceServiceHealthCheck offenceServiceHealthCheck,
                                 PaymentServiceHealthCheck paymentServiceHealthCheck,
                                 @ConfigurationValue(key = "securityService.bruteForceCheckingEnabled") boolean bruteForceProtectionEnabled,
                                 Provider<SecurityServiceHealthCheck> securityServiceHealthCheckProvider,
                                 @ConfigurationValue(key = "audit.enabled") boolean auditingEnabled,
                                 Provider<AuditMessageBrokerHealthCheck> auditMessageBrokerHealthCheckProvider) {

        register("deadlocks", new ThreadDeadlockHealthCheck());
        register("session-database", sessionDatabaseHealthCheck);
        register("offence-service", offenceServiceHealthCheck);
        register("payment-service", paymentServiceHealthCheck);
        if (bruteForceProtectionEnabled) {
            register("security-service", securityServiceHealthCheckProvider.get());
        }
        if (auditingEnabled) {
            register("audit-message-broker", auditMessageBrokerHealthCheckProvider.get());
        }
    }

}
