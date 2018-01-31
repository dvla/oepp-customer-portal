package controllers;

import org.junit.Test;
import play.mvc.Http;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.contentAsString;

public class PingControllerTest extends BaseControllerTest {

    @Test
    public void ping() throws Exception {
        makeRequest(HTTP.get("/ping"), (result) -> {
            assertThat(result.status(), is(Http.Status.OK));
            assertThat(result.contentType().get(), is(Http.MimeTypes.TEXT));
            assertThat(contentAsString(result), is("pong"));
        });
    }

}