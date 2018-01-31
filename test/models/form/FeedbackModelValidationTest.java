package models.form;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.RandomStringUtils.randomAscii;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the validation of the {@link FeedbackModel}.
 */
public class FeedbackModelValidationTest {

    private static final String VALID_NAME = "John Smith";
    private static final String VALID_EMAIL = "user@exampl.com";
    private static final String INVALID_EMAIL = "some invalid email";
    private static final int CHARACTERS_LIMIT = 500;

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void shouldPermitEmptyName() {
        FeedbackModel model = new FeedbackModel()
                .setEmail(VALID_EMAIL)
                .setMessage(randomAscii(CHARACTERS_LIMIT));

        Set<ConstraintViolation<FeedbackModel>> violations = validator.validate(model);
        assertThat(violations, is(Matchers.empty()));
    }

    @Test
    public void shouldPermitEmptyEmail() {
        FeedbackModel model = new FeedbackModel()
                .setName(VALID_NAME)
                .setMessage(randomAscii(CHARACTERS_LIMIT));

        Set<ConstraintViolation<FeedbackModel>> violations = validator.validate(model);
        assertThat(violations, is(Matchers.empty()));

    }

    @Test
    public void shouldForbidBlankMessages() {
        FeedbackModel model = new FeedbackModel()
                .setName(VALID_NAME)
                .setEmail(VALID_EMAIL)
                .setMessage(" ");

        Set<ConstraintViolation<FeedbackModel>> violations = validator.validate(model);
        assertThat(extractMessages(violations), containsInAnyOrder("error.required.feedback.content"));
    }

    @Test
    public void shouldForbidMessageLongerThen500Characters() {
        FeedbackModel model = new FeedbackModel()
                .setName(VALID_NAME)
                .setEmail(VALID_EMAIL)
                .setMessage(randomAscii(CHARACTERS_LIMIT + 1));

        Set<ConstraintViolation<FeedbackModel>> violations = validator.validate(model);
        assertThat(extractMessages(violations), containsInAnyOrder("error.invalid.feedback.content"));
    }

    @Test
    public void shouldForbidInvalidEmailIfProvided() {
        FeedbackModel model = new FeedbackModel()
                .setName(VALID_NAME)
                .setEmail(INVALID_EMAIL)
                .setMessage(randomAscii(CHARACTERS_LIMIT));

        Set<ConstraintViolation<FeedbackModel>> violations = validator.validate(model);
        assertThat(extractMessages(violations), containsInAnyOrder("error.invalid.feedback.email"));
    }

    private <T> List<String> extractMessages(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
    }

}
