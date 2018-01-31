package auditing;

import auditing.messages.AbstractAuditMessage;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.io.Resources;
import models.PaymentReceipt;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import services.result.GetCaseResult;
import services.result.InitiatePaymentResult;
import services.result.TransactionResult;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.domain.data.CaseData;
import uk.gov.dvla.domain.data.CaseRejection;
import uk.gov.dvla.domain.data.CaseRejection.CaseRejectionReason;
import uk.gov.dvla.domain.data.VehicleData;
import uk.gov.dvla.oepp.domain.payment.InitiateOffencePaymentResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static java.nio.charset.Charset.defaultCharset;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static auditing.AuditMessageFactory.*;

@RunWith(Parameterized.class)
public class AuditMessageFactoryTest {

    private static final Long CASE_NUMBER = 11110002L;
    private static final String VEHICLE_REGISTRATION_MARK = "CV03AAA";
    private static final String CASE_TYPE = "7A";
    private static final String CASE_TYPE_S29 = "29KK";
    private static final DateTime CASE_CLOSED_DATE = new DateTime().withYear(2010).withMonthOfYear(3).withDayOfMonth(2).withTimeAtStartOfDay();

    private static final BigDecimal ARREARS_AMOUNT = BigDecimal.valueOf(10.34);
    private static final BigDecimal PENALTY_AMOUNT = BigDecimal.valueOf(20.34);
    private static final BigDecimal ARREARS_AMOUNT_S29 = BigDecimal.valueOf(75.13);
    private static final BigDecimal PENALTY_AMOUNT_S29 = BigDecimal.ZERO;
    private static final BigDecimal VOLUNTARY_ARREARS_PAID = BigDecimal.ZERO;

    private static final Date TIMESTAMP = new DateTime().withYear(2016).withMonthOfYear(5).withDayOfMonth(23).withHourOfDay(9).withMinuteOfHour(32).withSecondOfMinute(12).toDate();
    private static final Date ARREARS_START_DATE = new DateTime().withTimeAtStartOfDay().withYear(2015).withMonthOfYear(10).withDayOfMonth(5).toDate();
    private static final Date ARREARS_END_DATE = new DateTime().withTimeAtStartOfDay().withYear(2016).withMonthOfYear(2).withDayOfMonth(12).toDate();
    private static final Interval ARREARS_INTERVAL = new Interval(new DateTime(ARREARS_START_DATE), new DateTime(ARREARS_END_DATE));

    private static final String EMAIL = "test@test.com";
    private static final boolean RECEIPT_REQUIRED = true;
    private static final PaymentReceipt PAYMENT_RECEIPT = new PaymentReceipt(RECEIPT_REQUIRED, EMAIL);

    private static final Long PAYMENT_ID = 1234L;

    @Parameterized.Parameters
    public static Object[][] testData() {
        return new Object[][]{
                {createAuditMessageOnCaseFound(getOpenCaseResult()), "offenceRecordFoundOpen7ACase.xml"},
                {createAuditMessageOnCaseFound(getClosedCaseResult(CaseRejectionReason.PENALTY_ALREADY_PAID)), "offenceRecordFoundAlreadyPaid.xml"},
                {createAuditMessageOnCaseFound(getClosedCaseResult(CaseRejectionReason.NO_PAYMENT_REQUIRED)), "offenceRecordFoundNoPaymentRequired.xml"},
                {createAuditMessageOnCaseFound(getClosedCaseResult(CaseRejectionReason.PASSED_TO_DEBT_COLLECTORS)), "offenceRecordFoundPassedToDebtCollectors.xml"},
                {createAuditMessageOnCaseFound(getClosedCaseResult(CaseRejectionReason.PASSED_TO_COURT)), "offenceRecordFoundPassedToCourt.xml"},
                {createAuditMessageOnCaseFound(getClosedCaseResult(CaseRejectionReason.INVALID_STATE)), "offenceRecordFoundInvalidState.xml"},
                {createAuditMessageOnCaseNotFound(new GetCaseResult.NotFound(), CASE_NUMBER, VEHICLE_REGISTRATION_MARK), "offenceRecordErrorCaseNotFound.xml"},
                {createAuditMessageOnCaseNotFound(new GetCaseResult.VehicleRegistrationMarkMismatch(), CASE_NUMBER, VEHICLE_REGISTRATION_MARK), "offenceRecordErrorVRMMismatch.xml"},
                {createAuditMessageOnCaseNotFound(new GetCaseResult.NotSupportedCaseType(), CASE_NUMBER, VEHICLE_REGISTRATION_MARK), "offenceRecordErrorNotSupportedCaseType.xml"},
                {createAuditMessageOnCaseNotFound(new GetCaseResult.Error(), CASE_NUMBER, VEHICLE_REGISTRATION_MARK), "offenceRecordErrorServiceUnavailable.xml"},
                {createAuditMessagePenaltyAccepted(getOffence()), "penaltyAccepted.xml"},
                {createAuditMessagePaymentInitiation(new InitiatePaymentResult.Success(new InitiateOffencePaymentResponse.Builder().setPaymentID(PAYMENT_ID).setPaymentReference("CASEREF").setPaymentPageUrl("/").create()), getOffence(), Optional.of(PAYMENT_RECEIPT)), "paymentInitiated.xml"},
                {createAuditMessagePaymentInitiation(new InitiatePaymentResult.Error(), getOffence(), Optional.of(PAYMENT_RECEIPT)), "paymentInitiationError.xml"},
                {createAuditMessageTransactionSuccessful(new TransactionResult.Success(), getOffence(),  Optional.of(PAYMENT_RECEIPT), PAYMENT_ID, "SENT"), "transactionSuccessful.xml"},
                {createAuditMessageTransactionError(new TransactionResult.NotAuthorised(), getOffence(),  Optional.of(PAYMENT_RECEIPT), PAYMENT_ID), "transactionErrorNotAuthorised.xml"},
                {createAuditMessageTransactionError(new TransactionResult.Error(), getOffence(),  Optional.of(PAYMENT_RECEIPT), PAYMENT_ID), "transactionErrorPaymentError.xml"},
                {createAuditMessageOnCaseFound(getOpenS29CaseResult()), "offenceRecordFoundOpenS29Case.xml"},
                {createAuditMessagePaymentInitiation(new InitiatePaymentResult.Success(new InitiateOffencePaymentResponse.Builder().setPaymentID(PAYMENT_ID).setPaymentReference("CASEREF").setPaymentPageUrl("/").create()), getS29Offence(), Optional.of(PAYMENT_RECEIPT)), "paymentInitiatedS29.xml"},
                {createAuditMessagePaymentInitiation(new InitiatePaymentResult.Error(), getS29Offence(), Optional.of(PAYMENT_RECEIPT)), "paymentInitiationErrorS29.xml"},
                {createAuditMessagePenaltyAccepted(getS29Offence()), "penaltyAcceptedS29.xml"},
                {createAuditMessageTransactionError(new TransactionResult.NotAuthorised(), getS29Offence(),  Optional.of(PAYMENT_RECEIPT), PAYMENT_ID), "transactionErrorNotAuthorisedS29.xml"},
                {createAuditMessageTransactionError(new TransactionResult.Error(), getS29Offence(),  Optional.of(PAYMENT_RECEIPT), PAYMENT_ID), "transactionErrorPaymentErrorS29.xml"},
                {createAuditMessageTransactionSuccessful(new TransactionResult.Success(), getS29Offence(),  Optional.of(PAYMENT_RECEIPT), PAYMENT_ID, "SENT"), "transactionSuccessfulS29.xml"}
        };
    }

    private XmlMapper mapper;
    private AbstractAuditMessage message;
    private String fixtureName;

    public AuditMessageFactoryTest(AbstractAuditMessage message, String fixtureName) {
        this.message = message;
        this.fixtureName = fixtureName;
    }

    @Before
    public void init() {
        mapper = new XmlMapper();
        mapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
    }

    @Test
    public void messageSerialisationTest() throws IOException {
        message.setDateTimeStamp(TIMESTAMP);

        assertThat(mapper.writeValueAsString(message), is(resourceAsString("fixtures/auditMessages/" + fixtureName)));
    }

    private static Offence getOffence() {
        Offence.Builder offenceBuilder = new Offence.Builder();
        offenceBuilder.setCaseData(new CaseData.Builder()
                .setPenaltyAmount(PENALTY_AMOUNT)
                .setArrearsInterval(ARREARS_INTERVAL)
                .setArrearsAmount(ARREARS_AMOUNT)
                .setPayableArrearsAmount(ARREARS_AMOUNT)
                .setVoluntaryArrearsPaid(VOLUNTARY_ARREARS_PAID)
                .setCaseType(CASE_TYPE)
                .create());
        offenceBuilder.setVehicleData(new VehicleData.Builder()
                .setTaxed(true).create());
        offenceBuilder.setCriteria(new Offence.Criteria(CASE_NUMBER, VEHICLE_REGISTRATION_MARK));
        return offenceBuilder.create();
    }

    private static Offence getS29Offence() {
        Offence.Builder offenceBuilder = new Offence.Builder();
        offenceBuilder.setCaseData(new CaseData.Builder()
                .setPenaltyAmount(PENALTY_AMOUNT_S29)
                .setArrearsInterval(ARREARS_INTERVAL)
                .setArrearsAmount(ARREARS_AMOUNT_S29)
                .setPayableArrearsAmount(ARREARS_AMOUNT_S29)
                .setVoluntaryArrearsPaid(VOLUNTARY_ARREARS_PAID)
                .setCaseType(CASE_TYPE_S29)
                .create());
        offenceBuilder.setVehicleData(new VehicleData.Builder()
                .setTaxed(true).create());
        offenceBuilder.setCriteria(new Offence.Criteria(CASE_NUMBER, VEHICLE_REGISTRATION_MARK));
        return offenceBuilder.create();
    }

    public static GetCaseResult.Found getOpenCaseResult() {
        return new GetCaseResult.Found(getOffence());
    }

    public static GetCaseResult.Found getOpenS29CaseResult() {
        return new GetCaseResult.Found(getS29Offence());
    }

    private static GetCaseResult.Found getClosedCaseResult(CaseRejectionReason rejectionCode) {
        Offence.Builder offenceBuilder = new Offence.Builder();
        offenceBuilder.setCaseRejection(new CaseRejection.Builder()
                .setRejectionReason(rejectionCode)
                .setCaseClosureDate(rejectionCode != CaseRejectionReason.INVALID_STATE ? CASE_CLOSED_DATE : null)
                .create()
        );
        offenceBuilder.setCriteria(new Offence.Criteria(CASE_NUMBER, VEHICLE_REGISTRATION_MARK));
        return new GetCaseResult.Found(offenceBuilder.create());
    }

    private static String resourceAsString(String name) throws IOException {
        return Resources.toString(Resources.getResource(name), defaultCharset());
    }

}
