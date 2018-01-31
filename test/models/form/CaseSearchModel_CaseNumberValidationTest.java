package models.form;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static models.form.FormValidationTestUtils.extractMessage;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the validation of the {@link CaseSearchModel#caseNumber}.
 */
@RunWith(Parameterized.class)
public class CaseSearchModel_CaseNumberValidationTest {

    @Parameterized.Parameters
    public static Object[][] testData() {
        final Long INVALID = 0L;
        final Long VALID = 12345678L;

        final String REQUIRED_VALIDATION_MESSAGE_KEY = "error.required.caseNumber";
        final String INVALID_VALIDATION_MESSAGE_KEY = "error.invalid.caseNumber";
        final String EMPTY_VALIDATION_MESSAGE_KEY = "";

        return new Object[][]{
                {null, REQUIRED_VALIDATION_MESSAGE_KEY},
                {INVALID, INVALID_VALIDATION_MESSAGE_KEY},
                {VALID, EMPTY_VALIDATION_MESSAGE_KEY}
        };
    }

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Long caseNumber;
    private String expectedValidationMessage;

    public CaseSearchModel_CaseNumberValidationTest(Long caseNumber, String expectedValidationMessage) {
        this.caseNumber = caseNumber;
        this.expectedValidationMessage = expectedValidationMessage;
    }

    @Test
    public void validateCaseNumber() {
        CaseSearchModel searchCase = new CaseSearchModel();
        searchCase.setCaseNumber(caseNumber);
        searchCase.setVehicleRegistrationMark("AB12CDE");

        Set<ConstraintViolation<CaseSearchModel>> violations = validator.validate(searchCase);
        assertThat(extractMessage(violations), is(expectedValidationMessage));
    }

}
