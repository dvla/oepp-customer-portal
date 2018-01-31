package metrics.health.checks.base;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class AbstractServiceHealthCheck extends HealthCheck {

    private final WSClient client;
    private long timeoutInMilliseconds = 2000;

    protected AbstractServiceHealthCheck(WSClient client) {
        this.client = client;
    }

    protected void setTimeout(long timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    @Override
    protected Result check() throws Exception {
        return client.url(healthCheckURL()).get().thenApplyAsync(this::evaluateHealth).exceptionally(Result::unhealthy)
                .toCompletableFuture().get(timeoutInMilliseconds, TimeUnit.MILLISECONDS);
    }

    protected abstract String healthCheckURL();

    private Result evaluateHealth(WSResponse response) {
        return response.getStatus() == Http.Status.OK ? Result.healthy() : Result.unhealthy("Health check returned: " + body(response));
    }

    private String body(WSResponse response) {
        String body = response.getBody();
        if (body == null) {
            return null;
        }
        switch (contentType(response)) {
            case "application/json":
                return replaceDoubleQuotes(body);
            case "text/plain":
                return joinNotBlankLines(body);
            default:
                return body;
        }
    }

    private String contentType(WSResponse response) {
        String contentType = response.getHeader("Content-Type");
        if (contentType.contains(";")) { // remove character encoding
            contentType = contentType.substring(0, contentType.indexOf(";"));
        }
        return contentType;
    }

    private String replaceDoubleQuotes(String body) {
        return body.replace("\"", "'");
    }

    private String joinNotBlankLines(String body) {
        try {
            List<String> lines = CharStreams.readLines(new StringReader(body));
            return lines.stream().map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.joining(", "));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
