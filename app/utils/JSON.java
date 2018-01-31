package utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import play.libs.Json;

public class JSON {

    /**
     * Writes the object to a format which is readable by humans (although not pretty).
     *
     * writeValueAsString has been used rather than toJson due to an issue with JacksonJson valueToTree which cannot
     * handle BigDecimals as plain values and instead converts them to scientific notation
     * @param value Value to stringify
     * @return JSON string of value
     */
    public static <T> String stringify(T value) {
        try {
            return Json.mapper().writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException(ex);
        }
    }

}
