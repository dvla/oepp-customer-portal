package controllers;

import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;

import java.net.URI;

public class InternationalizationController extends Controller {

    public Result changeLanguage(String code, String page) {
        if (!isRedirectSafe(page)) {
            Logger.debug("Unsafe redirect to {} has been detected - redirecting to page not found page", page);
            return redirect(routes.ErrorController.displayPageNotFoundErrorPage());
        }

        if (!changeLang(code)) {
            Logger.debug("Unsupported language {} has been chosen - redirecting to page not found page", code);
            return redirect(routes.ErrorController.displayPageNotFoundErrorPage());
        }

        Logger.debug("Changed UI language to {}", code);
        return redirect(appendLeadingSlash(page));
    }

    private boolean isRedirectSafe(String page) {
        URI redirectURI = URI.create(page);
        return !redirectURI.isAbsolute();
    }

    private String appendLeadingSlash(String page) {
        if (page.startsWith("/")) {
            return page;
        }
        return "/" + page;
    }

}
