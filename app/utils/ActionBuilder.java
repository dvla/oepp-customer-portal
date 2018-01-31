package utils;

import com.google.common.base.Preconditions;
import play.filters.csrf.CSRF;
import play.filters.csrf.CSRFConfig;
import play.mvc.Call;
import play.mvc.Http;

import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;

public class ActionBuilder {

    private Map<String, String> parameters;

    public ActionBuilder() {
        this.parameters = newHashMap();
    }

    public ActionBuilder withToken(CSRFConfig tokenConfig) {
        checkNotNull(tokenConfig, "Token configuration is required");

        String token = CSRF.getToken(context().request()).get().value();
        if (token != null) {
            parameters.put(tokenConfig.tokenName(), token);
        }
        return this;
    }

    public ActionBuilder withSessionID() {
        String sessionID = context().session().get("id");
        if (sessionID != null) {
            parameters.put("session", sessionID);
        }
        return this;
    }

    public URL build(Call action) {
        checkNotNull(action, "Action is required");

        String absoluteURL = action.absoluteURL(context().request());

        String extraQueryStringJoint = absoluteURL.contains("?") ? "&" : "?";

        String extraQueryString = parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        return URLParser.parse(absoluteURL + extraQueryStringJoint + extraQueryString);
    }

    private Http.Context context() {
        return Http.Context.current();
    }

}
