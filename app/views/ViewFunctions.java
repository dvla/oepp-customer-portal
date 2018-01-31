package views;

import play.data.validation.ValidationError;
import play.i18n.Messages;
import play.mvc.Http;

import java.util.Locale;

public class ViewFunctions {

    /**
     * Translates a message from {@link ValidationError}.
     * <p>
     * In case validation error object has more then one message key ({@link ValidationError#messages}), then
     * first one defined in i18n dictionary is translated.
     * <p>
     * In case none of message keys is defined in i18n dictionary,
     * then last untranslated message key is returned which reflects default behaviour of {@link Messages} class.
     *
     * @param error validation error
     * @return the formatted message or a last message key if the none was defined in i18n dictionary
     */
    public static String getMessage(ValidationError error) {
        return error.messages().stream().filter(Messages::isDefined).map(Messages::get).findFirst().orElse(error.message());
    }

    /**
     * Returns current locale used by user.
     * @return the current user locale
     */
    public static Locale userLocale() {
        return Http.Context.current().lang().toLocale();
    }
}
