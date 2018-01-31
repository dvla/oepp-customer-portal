package framework.filters;

import akka.stream.Materializer;
import configuration.inject.ConfigurationValue;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Singleton
public class HttpsSecurityHeadersFilter extends Filter {

    static final String HEADER_NAME = "Strict-Transport-Security";

    @Inject
    private Configuration configuration;

    @Inject
    public HttpsSecurityHeadersFilter(Materializer materializer) {
        super(materializer);
    }

    @Override
    public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> nextFilter, Http.RequestHeader requestHeader) {
        return nextFilter.apply(requestHeader).thenApply(this::addSecurityHeader);
    }

    private Result addSecurityHeader(Result result) {
        return configuration.strictTransportSecurity
                .map(headerValue -> result.withHeader(HEADER_NAME, headerValue))
                .orElse(result);
    }

    static class Configuration {

        @Inject
        @ConfigurationValue(key = "play.filters.headers.strictTransportSecurity")
        private Optional<String> strictTransportSecurity;

    }

}
