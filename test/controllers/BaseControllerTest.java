package controllers;

import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BaseControllerTest extends WithApplication {

    protected GuiceApplicationBuilder builder;

    protected BaseControllerTest() {
        this.builder = new GuiceApplicationBuilder().configure(ImmutableMap.<String, Object>builder()
                .put("play.filters.csrf.token.sign", "false")
                .build()
        );
    }

    @Override
    protected play.Application provideApplication() {
        return builder.build();
    }

    @Before
    public void init() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    protected void makeRequest(Request request, Consumer<Result> function) {
        function.accept(
                Helpers.route(
                        Helpers.fakeRequest(request.method, request.path)
                                .header("Accept-Language", request.language)
                                .bodyForm(request.body)
                                .tags(request.tags)
                )
        );
    }

    protected static class Request {

        private final String method;
        private final String path;

        private final Map<String, String> body = new HashMap<>();
        private final Map<String, String> tags = new HashMap<>();

        private String language = "en";

        protected Request(String method, String path) {
            this.method = method;
            this.path = path;
        }

        protected Request withFormParameter(String name, String value) {
            body.put(name, value);
            return this;
        }

        protected Request withCSRFToken(String token) {
            tags.put("CSRF_TOKEN", token);
            return this;
        }

        protected Request withLanguage(String language) {
            this.language = language;
            return this;
        }
    }

    protected static class HTTP {

        protected static Request get(String path) {
            return new Request("GET", path);
        }

        protected static Request post(String path) {
            return new Request("POST", path);
        }

        protected static Request head(String path) { return new Request("HEAD", path); }

    }
}
