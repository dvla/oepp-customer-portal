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
 * Tests the validation of the {@link CaseSearchModel#vehicleRegistrationMark}.
 */
@RunWith(Parameterized.class)
public class CaseSearchModel_VehicleRegistrationMarkValidationTest {

    @Parameterized.Parameters
    public static Object[][] testData() {
        final String INVALID_REG_MARK = "9AAAA";
        final String VALID_REG_MARK = "AAA99";

        final String REQUIRED_VALIDATION_MESSAGE_KEY = "error.required.vehicleRegistrationMark";
        final String INVALID_VALIDATION_MESSAGE_KEY = "error.invalid.vehicleRegistrationMark";
        final String EMPTY_VALIDATION_MESSAGE_KEY = "";

        return new Object[][]{
                {null, REQUIRED_VALIDATION_MESSAGE_KEY},
                {INVALID_REG_MARK, INVALID_VALIDATION_MESSAGE_KEY},
                {VALID_REG_MARK, EMPTY_VALIDATION_MESSAGE_KEY}
        };
    }

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private String vehicleRegistrationMark;
    private String expectedValidationMessage;

    public CaseSearchModel_VehicleRegistrationMarkValidationTest(String vehicleRegistrationMark, String expectedValidationMessage) {
        this.vehicleRegistrationMark = vehicleRegistrationMark;
        this.expectedValidationMessage = expectedValidationMessage;
    }

    @Test
    public void validateVehicleRegistrationMark() {
        CaseSearchModel searchCase = new CaseSearchModel();
        searchCase.setCaseNumber(12345678L);
        searchCase.setVehicleRegistrationMark(vehicleRegistrationMark);

        Set<ConstraintViolation<CaseSearchModel>> violations = validator.validate(searchCase);
        assertThat(extractMessage(violations), is(expectedValidationMessage));
    }

}
