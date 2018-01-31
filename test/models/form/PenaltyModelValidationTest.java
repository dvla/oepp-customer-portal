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

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests the validation of the {@link PenaltyModel}.
 */
public class PenaltyModelValidationTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    public void shouldForbidUntaxedVehiclesWhichHasNotBeenAcknowledged() {
        PenaltyModel model = new PenaltyModel()
                .setUntaxedVehicleAcknowledged(false);

        Set<ConstraintViolation<PenaltyModel>> violations = validator.validate(model);
        assertThat(extractMessages(violations), containsInAnyOrder("error.required.untaxedVehicleAcknowledge"));
    }

    @Test
    public void shouldPermitUntaxedVehiclesWhichHasBeenAcknowledged() {
        PenaltyModel model = new PenaltyModel()
                .setUntaxedVehicleAcknowledged(true);

        Set<ConstraintViolation<PenaltyModel>> violations = validator.validate(model);
        assertThat(violations, is(Matchers.empty()));
    }

    private <T> List<String> extractMessages(Set<ConstraintViolation<T>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toList());
    }

}
