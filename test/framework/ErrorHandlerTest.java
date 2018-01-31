package framework;

import exceptions.IllegalSessionStateException;
import org.junit.Before;
import org.junit.Test;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.Optional;
import java.util.concurrent.CompletionException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.fakeRequest;

public class ErrorHandlerTest extends WithApplication {

    private final static Http.RequestImpl request = fakeRequest().build();

    private ErrorHandler errorHandler;

    @Before
    public void init() {
        errorHandler = app.injector().instanceOf(ErrorHandler.class);
        Http.Context.current.set(new Http.Context(request));
    }

    @Test
    public void shouldRedirectToStartPageWhenIllegalSessionStateExceptionIsThrown() throws Exception {
        // given
        IllegalSessionStateException sessionStateException = new IllegalSessionStateException("Session is required");
        // when
        Result result = errorHandler.onServerError(request, sessionStateException).toCompletableFuture().get();
        // then
        assertThat(result.status(), is(303));
        assertThat(result.redirectLocation(), is(Optional.of("/")));
    }

    @Test
    public void shouldRedirectToStartPageWhenExceptionCausedByIllegalSessionStateExceptionIsThrown() throws Exception {
        // given
        IllegalSessionStateException sessionStateException = new IllegalSessionStateException("Session is required");
        CompletionException exceptionCausedBySessionStateException  = new CompletionException(sessionStateException);
        // when
        Result result = errorHandler.onServerError(request, exceptionCausedBySessionStateException).toCompletableFuture().get();
        // then
        assertThat(result.status(), is(303));
        assertThat(result.redirectLocation(), is(Optional.of("/")));
    }

    @Test
    public void shouldReturn500WhenExceptionNotRelatedToSessionStateHandlingIsThrown() throws Exception {
        // given
        NullPointerException exception = new NullPointerException("Something is missing");
        // when
        Result result = errorHandler.onServerError(request, exception).toCompletableFuture().get();
        // then
        assertThat(result.status(), is(500));
        assertThat(result.redirectLocation(), is(Optional.empty()));
    }

}