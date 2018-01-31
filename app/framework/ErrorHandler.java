package framework;

import com.google.common.base.Throwables;
import exceptions.IllegalSessionStateException;
import play.Configuration;
import play.Environment;
import play.Logger;
import play.api.OptionalSourceMapper;
import play.api.UsefulException;
import play.api.routing.Router;
import play.http.DefaultHttpErrorHandler;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import session.SessionManager;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.redirect;

public class ErrorHandler extends DefaultHttpErrorHandler {

    @Inject
    private SessionManager sessionManager;

    @Inject
    public ErrorHandler(Configuration configuration, Environment environment, OptionalSourceMapper sourceMapper, Provider<Router> routes) {
        super(configuration, environment, sourceMapper, routes);
    }

    @Override
    protected CompletionStage<Result> onBadRequest(Http.RequestHeader request, String message) {
        return CompletableFuture.completedFuture(redirect(controllers.routes.ErrorController.displayServiceUnavailableErrorPage()));
    }

    @Override
    protected CompletionStage<Result> onForbidden(Http.RequestHeader request, String message) {
        return CompletableFuture.completedFuture(redirect(controllers.routes.ErrorController.displayServiceUnavailableErrorPage()));
    }

    @Override
    protected CompletionStage<Result> onNotFound(Http.RequestHeader request, String message) {
        return CompletableFuture.completedFuture(redirect(controllers.routes.ErrorController.displayPageNotFoundErrorPage()));
    }

    @Override
    protected CompletionStage<Result> onOtherClientError(Http.RequestHeader request, int statusCode, String message) {
        return CompletableFuture.completedFuture(redirect(controllers.routes.ErrorController.displayServiceUnavailableErrorPage()));
    }

    @Override
    public CompletionStage<Result> onServerError(Http.RequestHeader request, Throwable exception) {
        if (Throwables.getRootCause(exception) instanceof IllegalSessionStateException) {
            sessionManager.destroySession();
            Logger.debug("Invalid session state: {} - redirecting to start page", exception.getMessage());
            return CompletableFuture.completedFuture(Results.redirect(controllers.routes.ApplicationController.redirectToStartPage()));
        }
        return super.onServerError(request, exception);
    }

    @Override
    protected CompletionStage<Result> onProdServerError(Http.RequestHeader request, UsefulException exception) {
        return CompletableFuture.completedFuture(redirect(controllers.routes.ErrorController.displayServiceUnavailableErrorPage()));
    }

}
