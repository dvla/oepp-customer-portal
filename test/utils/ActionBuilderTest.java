package utils;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.api.mvc.Call;
import play.filters.csrf.CSRF;
import play.filters.csrf.CSRFConfig;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ActionBuilderTest extends WithApplication {

    private CSRFConfig tokenConfig;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ImmutableMap.<String, Object>builder()
                .put("play.filters.csrf.token.name", "token")
                .put("play.filters.csrf.token.sign", "false")
                .build()
        );
    }

    @Before
    public void init() throws Exception {
        tokenConfig = app.injector().instanceOf(CSRFConfig.class);
        Http.Context.current.set(new Http.Context(fakeRequestWithCsrfToken()));
    }

    private Http.RequestImpl fakeRequestWithCsrfToken() {
        return Helpers.fakeRequest("POST", "http://localhost/payment/start")
                .session("id", "session-123")
                .session("token", "token-987")
                .tag("CSRF_TOKEN_NAME", "token")
                .tag("CSRF_TOKEN", "token-987")
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void withToken_shouldThrowExceptionWhenTokenConfigIsNull() throws MalformedURLException {
        new ActionBuilder().withToken(null);
    }

    @Test(expected = NullPointerException.class)
    public void build_shouldThrowExceptionWhenActionIsNull() {
        new ActionBuilder().build(null);
    }

    @Test
    public void build_shouldAppendTokenToPath() throws MalformedURLException {
        URL url = new ActionBuilder().withToken(tokenConfig).build(callWithoutParameters());
        assertThat(url, is(new URL("http://localhost/payment/finish?token=token-987")));
    }

    @Test
    public void build_shouldAppendTokenToQueryString() throws MalformedURLException {
        URL url = new ActionBuilder().withToken(tokenConfig).build(callWithParameters());
        assertThat(url, is(new URL("http://localhost/payment/finish?action=search&token=token-987")));
    }

    @Test
    public void build_shouldAppendSessionIDToPath() throws MalformedURLException {
        URL url = new ActionBuilder().withSessionID().build(callWithoutParameters());
        assertThat(url, is(new URL("http://localhost/payment/finish?session=session-123")));
    }

    @Test
    public void build_shouldAppendSessionIDToQueryString() throws MalformedURLException {
        URL url = new ActionBuilder().withSessionID().build(callWithParameters());
        assertThat(url, is(new URL("http://localhost/payment/finish?action=search&session=session-123")));
    }

    @Test
    public void build_shouldProperJoinAllQueryParameters() throws MalformedURLException {
        URL url = new ActionBuilder().withToken(tokenConfig).withSessionID().build(callWithParameters());
        assertThat(url, is(new URL("http://localhost/payment/finish?action=search&session=session-123&token=token-987")));
    }

    private Call callWithoutParameters() {
        return new Call("POST", "/payment/finish", null);
    }

    private Call callWithParameters() {
        return new Call("POST", "/payment/finish?action=search", null);
    }

}