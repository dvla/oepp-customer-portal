package framework.filters;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import com.google.common.io.Resources;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import play.Logger;
import play.test.Helpers;
import play.test.WithApplication;

import java.io.IOException;

import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessLogFilterTest extends WithApplication {

    @Mock
    private OutputStreamAppender<ILoggingEvent> logAppender;
    @Captor
    private ArgumentCaptor<ILoggingEvent> logCaptor;

    @Before
    public void init() {
        DateTimeUtils.setCurrentMillisFixed(DateTime.parse("2016-06-02T13:56").getMillis());

        when(logAppender.getName()).thenReturn("ACCESS_LOG");
        ((ch.qos.logback.classic.Logger) Logger.of("http.request").underlying()).addAppender(logAppender);
    }

    @Test
    public void shouldProduceAccessLogWithCorrectFormat() throws IOException {
        Helpers.route(Helpers.fakeRequest("GET", "/"));

        verify(logAppender).doAppend(logCaptor.capture());
        assertThat(logCaptor.getValue().getFormattedMessage(), is(resourceAsString("fixtures/access-log-message.log")));
    }

    private String resourceAsString(String name) throws IOException {
        return Resources.toString(Resources.getResource(name), defaultCharset());
    }
}
