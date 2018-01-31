package metrics.health.reporting;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.json.HealthCheckModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import play.mvc.Http;
import play.twirl.api.Content;

import java.util.Map;

public class HealthCheckReport implements Content {

    private static final ObjectMapper mapper = new ObjectMapper().registerModule(new HealthCheckModule());

    private final Map<String, HealthCheck.Result> results;

    public HealthCheckReport(Map<String, HealthCheck.Result> results) {
        this.results = results;
    }

    @Override
    public String body() {
        try {
            return mapper.writeValueAsString(results);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String contentType() {
        return Http.MimeTypes.JSON;
    }
}
