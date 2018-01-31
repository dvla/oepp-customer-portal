package models.form;

import javax.validation.ConstraintViolation;
import java.util.Set;

final class FormValidationTestUtils {

    private FormValidationTestUtils() {
        throw new AssertionError("This class should not be instantiated.");
    }

    static String extractMessage(Set<ConstraintViolation<CaseSearchModel>> constraintViolations) {
        StringBuilder message = new StringBuilder("");

        for (ConstraintViolation<CaseSearchModel> searchCaseConstraintViolation : constraintViolations) {
            message.append(searchCaseConstraintViolation.getMessage());
        }

        return message.toString();
    }
}
