package metrics.health.checks;

import configuration.inject.ConfigurationValue;
import metrics.health.checks.base.AbstractServiceHealthCheck;
import play.libs.ws.WSClient;
import utils.URLParser;

import javax.inject.Inject;
import java.net.URISyntaxException;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class OffenceServiceHealthCheck extends AbstractServiceHealthCheck {

    private final URL serviceAdminURL;

    @Inject
    public OffenceServiceHealthCheck(WSClient client,
                                     @ConfigurationValue(key = "offenceService.adminURL") String serviceAdminURL,
                                     @ConfigurationValue(key = "metrics.healthcheck.timeout") int timeout) {
        super(client);

        checkArgument(!isNullOrEmpty(serviceAdminURL), "Offence service admin URL is required");
        checkArgument(timeout >= 0, "Health check timeout must be greater than or equal to zero");
        try {
            this.serviceAdminURL = URLParser.parse(serviceAdminURL);
        } catch (URLParser.ParsingException ex) {
            throw new IllegalArgumentException("Offence service admin URL is invalid", ex);
        }

        setTimeout(timeout);
    }

    @Override
    protected String healthCheckURL() {
        try {
            return serviceAdminURL.toURI().resolve("/healthcheck").toASCIIString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

}
