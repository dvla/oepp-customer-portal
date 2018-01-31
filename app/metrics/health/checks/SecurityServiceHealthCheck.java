package metrics.health.checks;

import configuration.inject.ConfigurationValue;
import metrics.health.checks.base.AbstractServiceHealthCheck;
import play.libs.ws.WSClient;

import javax.inject.Inject;

public class SecurityServiceHealthCheck extends AbstractServiceHealthCheck {

    @Inject
    @ConfigurationValue(key = "securityService.adminURL")
    private String serviceAdminURL;

    @Inject
    public SecurityServiceHealthCheck(WSClient client, @ConfigurationValue(key = "metrics.healthcheck.timeout") int timeout) {
        super(client);
        setTimeout(timeout);
    }

    @Override
    protected String healthCheckURL() {
        return serviceAdminURL + "/healthcheck";
    }

}
