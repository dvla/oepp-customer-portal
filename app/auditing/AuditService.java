package auditing;

import akka.actor.ActorSystem;
import auditing.messages.AbstractAuditMessage;
import configuration.inject.ConfigurationValue;
import play.Logger;
import uk.gov.dvla.oepp.audit.service.client.AuditClient;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Audit service client wrapper around the Audit Client.
 * Used to inject configuration values and actor system from Play.
 * Assists in decluttering
 */
public class AuditService {

    private AuditClient auditClient;
    private boolean auditEnabled;

    private final Logger.ALogger logger = Logger.of("audit-service");

    @Inject
    public AuditService(ActorSystem system,
                        @ConfigurationValue(key = "audit.message-broker.host") String host,
                        @ConfigurationValue(key = "audit.message-broker.port") int port,
                        @ConfigurationValue(key = "audit.message-broker.username") String username,
                        @ConfigurationValue(key = "audit.message-broker.password") String password,
                        @ConfigurationValue(key = "audit.message-broker.exchangeName") String exchangeName,
                        @ConfigurationValue(key = "audit.enabled") boolean auditEnabled) {

        this.auditEnabled = auditEnabled;

        if (auditEnabled) {
            logger.debug("Initialising audit client for host {}", host);
            auditClient = new AuditClient();

            try {
                auditClient.initialise(system, host, port, username, password, exchangeName);
            } catch (IOException ex) {
                logger.error("Audit service client failed to connect", ex);
            } catch (TimeoutException ex) {
                logger.error("Audit service client connection timeout", ex);
            }
        } else {
            logger.warn("Auditing is disabled");
        }
    }

    private void sendAuditMessage(AbstractAuditMessage message) {
        auditClient.sendAuditMessage(message);
        logger.debug("Sent audit message for page transition: {}", message.getPageTransition());
    }

    public <T extends AbstractAuditMessage> void sendAuditMessage(Supplier<T> messageSupplier) {
        if (auditEnabled) {
            sendAuditMessage(messageSupplier.get());
        }
    }
}
