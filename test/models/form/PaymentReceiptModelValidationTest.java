package models.form;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Tests the validation of the {@link PaymentReceiptModel}.
 */
@RunWith(Parameterized.class)
public class PaymentReceiptModelValidationTest {

    private static Validator validator;

    private PaymentReceiptModel model;
    private List<String> violationMessages;

    @BeforeClass
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public PaymentReceiptModelValidationTest(PaymentReceiptModel model, List<String> violationMessages) {
        this.model = model;
        this.violationMessages = violationMessages;
    }

    @SuppressWarnings("ConstantConditions")
    @Parameterized.Parameters(name = "{0}")
    public static Object[][] testData() {
        boolean WANT_EMAIL_RECEIPT = true;
        boolean DO_NOT_WANT_EMAIL_RECEIPT = false;

        String BLANK_EMAIL = "";
        String INVALID_EMAIL = "j.smith";
        String VALID_EMAIL = "j.smith@foo.com";
        String ANOTHER_VALID_EMAIL = "j.smith@bar.com";

        String EMAIL_RECEIPT_DECISION_REQUIRED = "required.emailReceiptDecision";
        String EMAIL_REQUIRED = "required.email";
        String EMAIL_INVALID = "invalid.email";
        String REPEATED_EMAIL_REQUIRED = "required.repeatedEmail";
        String REPEATED_EMAIL_INVALID = "invalid.repeatedEmail";
        String REPEATED_EMAIL_MISMATCH = "mismatch.repeatedEmail";

        return new Object[][]{
                {model(null, null, null), violationMessages(EMAIL_RECEIPT_DECISION_REQUIRED)},
                {model(WANT_EMAIL_RECEIPT, null, null), violationMessages(EMAIL_REQUIRED, REPEATED_EMAIL_REQUIRED)},
                {model(WANT_EMAIL_RECEIPT, BLANK_EMAIL, null), violationMessages(EMAIL_REQUIRED, REPEATED_EMAIL_REQUIRED)},
                {model(WANT_EMAIL_RECEIPT, null, BLANK_EMAIL), violationMessages(EMAIL_REQUIRED, REPEATED_EMAIL_REQUIRED)},
                {model(WANT_EMAIL_RECEIPT, BLANK_EMAIL, BLANK_EMAIL), violationMessages(EMAIL_REQUIRED, REPEATED_EMAIL_REQUIRED)},
                {model(WANT_EMAIL_RECEIPT, INVALID_EMAIL, BLANK_EMAIL), violationMessages(EMAIL_INVALID, REPEATED_EMAIL_REQUIRED, REPEATED_EMAIL_MISMATCH)},
                {model(WANT_EMAIL_RECEIPT, BLANK_EMAIL, INVALID_EMAIL), violationMessages(EMAIL_REQUIRED, REPEATED_EMAIL_INVALID, REPEATED_EMAIL_MISMATCH)},
                {model(WANT_EMAIL_RECEIPT, VALID_EMAIL, BLANK_EMAIL), violationMessages(REPEATED_EMAIL_REQUIRED, REPEATED_EMAIL_MISMATCH)},
                {model(WANT_EMAIL_RECEIPT, BLANK_EMAIL, VALID_EMAIL), violationMessages(EMAIL_REQUIRED, REPEATED_EMAIL_MISMATCH)},
                {model(WANT_EMAIL_RECEIPT, VALID_EMAIL, ANOTHER_VALID_EMAIL), violationMessages(REPEATED_EMAIL_MISMATCH)},
                {model(WANT_EMAIL_RECEIPT, ANOTHER_VALID_EMAIL, VALID_EMAIL), violationMessages(REPEATED_EMAIL_MISMATCH)},
                {model(WANT_EMAIL_RECEIPT, VALID_EMAIL, VALID_EMAIL), violationMessages()},
                {model(DO_NOT_WANT_EMAIL_RECEIPT, null, null), violationMessages()}
        };
    }

    private static PaymentReceiptModel model(Boolean emailReceiptDecision, String email, String repeatedEmail) {
        PaymentReceiptModel model = new PaymentReceiptModel();
        model.setEmailReceiptDecision(emailReceiptDecision);

        PaymentReceiptModel.Emails fields = new PaymentReceiptModel.Emails();
        fields.setEmail(email);
        fields.setRepeatedEmail(repeatedEmail);

        model.setReceiptEmails(fields);
        return model;
    }

    private static List<String> violationMessages(String... messages) {
        return Arrays.asList(messages).stream().map((message) -> "error." + message).collect(Collectors.toList());
    }

    @Test
    public void validateCaseNumber() {
        Set<ConstraintViolation<PaymentReceiptModel>> violations = validator.validate(model);
        assertThat(extractMessages(violations), containsInAnyOrder(this.violationMessages.toArray()));
    }

    private <T> List<String> extractMessages(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
    }

}
