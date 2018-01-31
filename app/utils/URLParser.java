package utils;


import java.net.MalformedURLException;
import java.net.URL;

public class URLParser {

    public static URL parse(String spec) {
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw new ParsingException(e);
        }
    }

    public static class ParsingException extends RuntimeException {
        private ParsingException(Throwable cause) {
            super(cause);
        }
    }
}
