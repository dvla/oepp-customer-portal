package views;

import com.google.common.collect.Lists;
import org.junit.Test;
import play.data.validation.ValidationError;
import play.test.WithApplication;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ViewFunctionsTest extends WithApplication {

    @Test
    public void getMessage_shouldTranslateFirstDefinedKey() throws Exception {
        ValidationError validationError = validationError("error.invalid", "first-unknown-property", "second-unknown-property");
        assertThat(ViewFunctions.getMessage(validationError), is("Invalid value"));
    }

    @Test
    public void getMessage_shouldSkipUndefinedKeys() throws Exception {
        ValidationError validationError = validationError("first-unknown-property", "error.invalid", "second-undefined-property");
        assertThat(ViewFunctions.getMessage(validationError), is("Invalid value"));
    }

    @Test
    public void getMessage_shouldReturnLastUntranslatedKeyWhenNothingIsDefined() throws Exception {
        ValidationError validationError = validationError("first-unknown-property", "last-unknown-property");
        assertThat(ViewFunctions.getMessage(validationError), is("last-unknown-property"));
    }

    private ValidationError validationError(String message, String... otherMessages) {
        return new ValidationError("field-x", Lists.asList(message, otherMessages), Lists.newArrayList());
    }
}