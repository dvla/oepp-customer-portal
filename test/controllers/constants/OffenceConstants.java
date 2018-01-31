package controllers.constants;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.domain.data.CaseData;
import uk.gov.dvla.domain.data.CaseRejection;
import uk.gov.dvla.domain.data.VehicleData;

import java.math.BigDecimal;
import java.util.Optional;

public class OffenceConstants {

    private static final BigDecimal PENALTY_AMOUNT = new BigDecimal(40.00);
    private static final BigDecimal ARREARS_AMOUNT = new BigDecimal(21.00);
    private static final BigDecimal PAYABLE_ARREARS_AMOUNT = new BigDecimal(24.00);
    private static final BigDecimal VOLUNTARY_ARREARS_PAID = new BigDecimal(11.00);
    private static final DateTime ARREARS_FROM = new DateTime().withYear(2016).withMonthOfYear(5).withDayOfMonth(14).withTimeAtStartOfDay();
    private static final DateTime ARREARS_TO = new DateTime().withYear(2016).withMonthOfYear(8).withDayOfMonth(31).withTimeAtStartOfDay();
    private static final Interval ARREARS_INTERVAL = new Interval(ARREARS_FROM, ARREARS_TO);

    public static final Long CASE_NUMBER = 22221111L;
    public static final String VEHICLE_REGISTRATION_MARK = "CV07BBB";
    private static final String CASE_TYPE_CIE = "144A";

    // The below methods create an offence that can be used in Junit tests
    public static Offence offence() {
        return offenceBuilder()
                .setCaseData(caseData())
                .setVehicleData(new VehicleData.Builder()
                        .setTaxed(true)
                        .setNewTaxStartDate(Optional.empty())
                        .create()
                ).create();
    }

    public static CaseData caseData() {
        return new CaseData.Builder()
                .setPenaltyAmount(PENALTY_AMOUNT)
                .setArrearsAmount(ARREARS_AMOUNT)
                .setPayableArrearsAmount(PAYABLE_ARREARS_AMOUNT)
                .setVoluntaryArrearsPaid(VOLUNTARY_ARREARS_PAID)
                .setArrearsInterval(ARREARS_INTERVAL)
                .create();
    }

    public static CaseData caseDataCIE() {
        return new CaseData.Builder()
                .setPenaltyAmount(PENALTY_AMOUNT)
                .setArrearsAmount(ARREARS_AMOUNT)
                .setPayableArrearsAmount(PAYABLE_ARREARS_AMOUNT)
                .setVoluntaryArrearsPaid(VOLUNTARY_ARREARS_PAID)
                .setArrearsInterval(ARREARS_INTERVAL)
                .setCaseType(CASE_TYPE_CIE)
                .create();
    }

    public static Offence offence(CaseRejection.CaseRejectionReason rejectionReason) {
        return offenceBuilder()
                .setCaseRejection(new CaseRejection.Builder()
                        .setRejectionReason(rejectionReason)
                        .setCaseType(CASE_TYPE_CIE)
                        .create())
                .create();
    }

    public static Offence.Builder offenceBuilder() {
        return new Offence.Builder().setCriteria(new Offence.Criteria(CASE_NUMBER, VEHICLE_REGISTRATION_MARK));
    }
}
