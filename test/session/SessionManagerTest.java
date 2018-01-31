package session;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.fakeRequest;
import static session.SessionManagerTest.RegexMatcher.matchesRegex;

public class SessionManagerTest extends WithApplication {

    private static final String SESSION_ID = "my-session-id";

    private SessionManager sessionManager;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ImmutableMap.<String, Object>builder().build());
    }

    @Before
    public void init() {
        sessionManager = app.injector().instanceOf(SessionManager.class);
    }

    @Test
    public void session_shouldReadSessionIdentifierFromCookie() {
        setContext(fakeRequest().session("id", SESSION_ID));

        assertThat(sessionManager.session().id(), is(SESSION_ID));
    }

    @Test
    public void session_shouldReadSessionIdentifierFromQueryParameters() {
        setContext(fakeRequest().uri("?session=" + SESSION_ID));

        assertThat(sessionManager.session().id(), is(SESSION_ID));
    }

    @Test
    public void session_queryStringShouldTakePrecedenceOverCookieWhenReadingSessionIdentifier() {
        setContext(fakeRequest().uri("?session=" + SESSION_ID).session("id", "another-session-id"));

        assertThat(sessionManager.session().id(), is(SESSION_ID));
    }

    @Test
    public void session_shouldCreateNewSessionIdentifierWhenNoneExists() {
        setContext(fakeRequest());

        assertThat(sessionManager.session().id(), matchesRegex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    public void destroySession_shouldRemoveSessionIdentifierFromCookie() {
        setContext(fakeRequest().session("id", SESSION_ID));

        sessionManager.destroySession();

        assertThat(getContext().session().get("id"), nullValue());
    }

    private Http.Context getContext() {
        return Http.Context.current.get();
    }

    private void setContext(Http.RequestBuilder builder) {
        Http.Context.current.set(new Http.Context(builder));
    }

    static class RegexMatcher extends TypeSafeMatcher<String> {

        private final String regex;

        private RegexMatcher(String regex) {
            this.regex = regex;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("matches regex=`" + regex + "`");
        }

        @Override
        public boolean matchesSafely(String string) {
            return string.matches(regex);
        }

        static RegexMatcher matchesRegex(String regex) {
            return new RegexMatcher(regex);
        }
    }

}