package controllers;

import org.hamcrest.Matcher;
import org.junit.Test;
import play.api.mvc.Call;
import play.mvc.Http;
import play.mvc.Result;

import static controllers.routes.ApplicationController;
import static controllers.routes.ErrorController;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class InternationalizationControllerTest extends BaseControllerTest {

    @Test
    public void shouldNotSetLanguageCookieWhenTwoLetterLanguageCodeIsNotUsed() {
        makeRequest(
                HTTP.get("/eng/search"),
                (result) -> assertThat(languageCookieValue(result), nullValue())
        );
    }

    @Test
    public void shouldRedirectToPageNotFoundErrorPageWhenTwoLetterLanguageCodeIsNotUsed() {
        makeRequest(
                HTTP.get("/eng/search"),
                (result) -> assertThat(result.redirectLocation().get(), matchesControllerMethod(ErrorController.displayPageNotFoundErrorPage()))
        );
    }

    @Test
    public void shouldNotSetLanguageCookieWhenUnsupportedLanguageCodeIsUsed() {
        makeRequest(
                HTTP.get("/pl/search"),
                (result) -> assertThat(languageCookieValue(result), nullValue())
        );
    }

    @Test
    public void shouldRedirectToPageNotFoundErrorPageWhenUnsupportedLanguageCodeIsUsed() {
        makeRequest(
                HTTP.get("/pl/search"),
                (result) -> assertThat(result.redirectLocation().get(), matchesControllerMethod(ErrorController.displayPageNotFoundErrorPage()))
        );
    }

    @Test
    public void shouldNotSetLanguageCookieWhenExternalRedirectIsDetected() {
        makeRequest(
                HTTP.get("/en/http://google.co.uk"),
                (result) -> assertThat(languageCookieValue(result), nullValue())
        );
    }

    @Test
    public void shouldRedirectToPageNotFoundErrorPageWhenExternalRedirectIsDetected() {
        makeRequest(
                HTTP.get("/en/http://google.co.uk"),
                (result) -> assertThat(result.redirectLocation().get(), matchesControllerMethod(ErrorController.displayPageNotFoundErrorPage()))
        );
    }

    @Test
    public void shouldSetEnglishLanguageCookieWhenEnglishLanguageCodeIsUsed() {
        makeRequest(
                HTTP.get("/en/search"),
                (result) -> assertThat(languageCookieValue(result), is("en"))
        );
    }

    @Test
    public void shouldRefreshPageWhenEnglishLanguageCodeIsUsed() {
        makeRequest(
                HTTP.get("/en/search"),
                (result) -> assertThat(result.redirectLocation().get(), matchesControllerMethod(ApplicationController.displaySearchCaseForm()))
        );
    }

    @Test
    public void shouldSetWelshLanguageCookieWhenWelshLanguageCodeIsUsed() {
        makeRequest(
                HTTP.get("/cy/search"),
                (result) -> assertThat(languageCookieValue(result), is("cy"))
        );
    }

    @Test
    public void shouldRefreshPageWhenWelshLanguageCodeIsUsed() {
        makeRequest(
                HTTP.get("/cy/search"),
                (result) -> assertThat(result.redirectLocation().get(), matchesControllerMethod(ApplicationController.displaySearchCaseForm()))
        );
    }

    private String languageCookieValue(Result result) {
        Http.Cookie cookie = result.cookie("PLAY_LANG");
        if (cookie == null) {
            return null;
        }
        return cookie.value();
    }

    private Matcher<String> matchesControllerMethod(Call call) {
        return is(call.url());
    }

}