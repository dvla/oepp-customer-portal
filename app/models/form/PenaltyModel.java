package models.form;

import javax.validation.constraints.AssertTrue;
import java.util.Objects;

public class PenaltyModel {

    @AssertTrue(message = "error.required.untaxedVehicleAcknowledge")
    private boolean untaxedVehicleAcknowledged;

    public boolean isUntaxedVehicleAcknowledged() {
        return untaxedVehicleAcknowledged;
    }

    public PenaltyModel setUntaxedVehicleAcknowledged(boolean untaxedVehicleAcknowledged) {
        this.untaxedVehicleAcknowledged = untaxedVehicleAcknowledged;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PenaltyModel that = (PenaltyModel) o;
        return untaxedVehicleAcknowledged == that.untaxedVehicleAcknowledged;
    }

    @Override
    public int hashCode() {
        return Objects.hash(untaxedVehicleAcknowledged);
    }
}
