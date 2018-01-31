package framework.filters;

import akka.stream.Materializer;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.OutputStreamAppender;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

@Singleton
public class AccessLogFilter extends Filter {

    private final play.Logger.ALogger logger = play.Logger.of("http.request");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("dd/MMM/yyyy:HH:mm:ss Z");

    @Inject
    public AccessLogFilter(Materializer materializer) {
        super(materializer);
        createAccessLogPattern();
    }

    private void createAccessLogPattern() {
        Logger logger = (Logger) this.logger.underlying();
        OutputStreamAppender<ILoggingEvent> appender = (OutputStreamAppender<ILoggingEvent>) logger.getAppender("ACCESS_LOG");

        if (appender == null) {
            appender = new ConsoleAppender<>();
            appender.setName("ACCESS_LOG");
        }

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(logger.getLoggerContext());
        encoder.setPattern("%message%n");
        appender.setEncoder(encoder);
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);

        encoder.start();
        appender.start();
    }

    @Override
    public CompletionStage<Result> apply(Function<Http.RequestHeader, CompletionStage<Result>> function, Http.RequestHeader requestHeader) {
        return function.apply(requestHeader).thenApply(result -> {
            logger.info("{} - - [{}] \"{} {} {}\" {} {} \"{}\" \"{}\"",
                    requestHeader.remoteAddress(),
                    dateTimeFormatter.print(DateTime.now()),
                    requestHeader.method(),
                    requestHeader.path(),
                    requestHeader.version(),
                    result.status(),
                    result.body().contentLength().orElse(0L),
                    stringifyHeaderValue(requestHeader, "Referer"),
                    stringifyHeaderValue(requestHeader, "User-Agent")
            );
            return result;
        });
    }

    private String stringifyHeaderValue(Http.RequestHeader requestHeader, String headerField) {
        return (requestHeader.hasHeader(headerField)) ? (requestHeader.getHeader(headerField)) : "-";
    }
}
