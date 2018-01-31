package models.form;

import com.google.common.base.MoreObjects;
import uk.gov.dvla.validation.constraints.LOCS;
import uk.gov.dvla.validation.constraints.VRM;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class CaseSearchModel {

    @NotNull(message = "error.required.caseNumber")
    @LOCS.CaseNumber(message = "error.invalid.caseNumber")
    private Long caseNumber;

    @NotNull(message = "error.required.vehicleRegistrationMark")
    @VRM(message = "error.invalid.vehicleRegistrationMark")
    private String vehicleRegistrationMark;

    public Long getCaseNumber() {
        return caseNumber;
    }

    public CaseSearchModel setCaseNumber(Long caseNumber) {
        this.caseNumber = caseNumber;
        return this;
    }

    public String getVehicleRegistrationMark() {
        return vehicleRegistrationMark;
    }

    public CaseSearchModel setVehicleRegistrationMark(String vehicleRegistrationMark) {
        this.vehicleRegistrationMark = vehicleRegistrationMark;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaseSearchModel that = (CaseSearchModel) o;
        return Objects.equals(caseNumber, that.caseNumber) &&
                Objects.equals(vehicleRegistrationMark, that.vehicleRegistrationMark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseNumber, vehicleRegistrationMark);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("caseNumber", caseNumber)
                .add("vehicleRegistrationMark", vehicleRegistrationMark)
                .toString();
    }
}
