package auditing;

import auditing.messages.*;
import models.PaymentReceipt;
import services.result.GetCaseResult;
import services.result.InitiatePaymentResult;
import services.result.TransactionResult;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.domain.data.CaseData;
import uk.gov.dvla.domain.data.CaseRejection;

import java.util.Optional;

/**
 * Constructs the various audit messages by page transition.
 */
public class AuditMessageFactory {

    public static OffenceRecordFound createAuditMessageOnCaseFound(GetCaseResult.Found result) {
        OffenceRecordFound auditMsg = new OffenceRecordFound(result.getPageMovement());
        Offence offence = result.getResponse();

        if (offence.getCaseRejection().isPresent()) {
            CaseRejection caseRejection = offence.getCaseRejection().get();
            auditMsg.setCaseRejectionCode(caseRejection.getRejectionReason().toString());
            caseRejection.getCaseClosureDate().ifPresent(dateTime -> auditMsg.setCaseClosedDate(dateTime.toDate()));
        } else {
            setCaseData(auditMsg, offence.getCaseData());
        }

        setCommonMessageData(auditMsg, offence);

        return auditMsg;
    }

    public static OffenceRecordError createAuditMessageOnCaseNotFound(GetCaseResult result, Long caseNumber, String registrationMark) {
        OffenceRecordError auditMsg = new OffenceRecordError(result.getPageMovement());

        auditMsg.setCaseNumber(caseNumber);
        auditMsg.setVehicleRegistrationMark(registrationMark);

        return auditMsg;
    }

    public static PenaltyAccepted createAuditMessagePenaltyAccepted(Offence offence) {
        PenaltyAccepted auditMsg = new PenaltyAccepted("penaltySummaryToPaymentReceipt");

        setCommonMessageData(auditMsg, offence);
        setCaseData(auditMsg, offence.getCaseData());

        return auditMsg;
    }

    public static AbstractAuditMessage createAuditMessagePaymentInitiation(InitiatePaymentResult result, Offence offence, Optional<PaymentReceipt> paymentReceipt) {
        AbstractAuditMessage auditMsg;
        if (result instanceof InitiatePaymentResult.Success) {
            auditMsg = new PaymentInitiated(result.getPageMovement());
        } else {
            auditMsg = new PaymentInitiationError(result.getPageMovement());
        }

        setCommonMessageData(auditMsg, offence);
        setCaseData((HasOffenceDetails) auditMsg, offence.getCaseData());
        setPaymentReceipt((HasReceiptDetails) auditMsg, paymentReceipt);
        return auditMsg;
    }

    public static TransactionSuccessful createAuditMessageTransactionSuccessful(TransactionResult.Success transactionResult, Offence offence, Optional<PaymentReceipt> paymentReceipt, Long paymentId, String emailSendResult) {
        TransactionSuccessful auditMsg = new TransactionSuccessful(transactionResult.getPageMovement());

        setCommonMessageData(auditMsg, offence);
        setCaseData(auditMsg, offence.getCaseData());
        setPaymentReceipt(auditMsg, paymentReceipt);

        auditMsg.setPaymentId(paymentId);
        offence.getCaseData().ifPresent(caseData -> auditMsg.setTotalAmountPaid(caseData.getPaymentAmount()));
        auditMsg.setEmailSendResult(emailSendResult);

        return auditMsg;
    }

    public static TransactionError createAuditMessageTransactionError(TransactionResult result, Offence offence, Optional<PaymentReceipt> paymentReceipt, Long paymentId) {
        TransactionError auditMsg = new TransactionError(result.getPageMovement());

        setCommonMessageData(auditMsg, offence);
        setCaseData(auditMsg, offence.getCaseData());
        setPaymentReceipt(auditMsg, paymentReceipt);
        auditMsg.setPaymentId(paymentId);
        offence.getCaseData().ifPresent(caseData -> auditMsg.setTotalAmountPaid(caseData.getPaymentAmount()));

        return auditMsg;
    }

    private static void setCaseData(HasOffenceDetails message, Optional<CaseData> data) {
        data.ifPresent(caseData -> {
            message.setPenaltyAmount(caseData.getPenaltyAmount());
            message.setArrearsAmount(caseData.getArrearsAmount());
            message.setVoluntaryArrearsAmount(caseData.getVoluntaryArrearsPaid());
            message.setArrearsTo(caseData.getArrearsInterval().getEnd().toDate());
        });
    }

    private static void setPaymentReceipt(HasReceiptDetails message, Optional<PaymentReceipt> paymentReceipt) {
        paymentReceipt.ifPresent(receipt -> {
            message.setReceiptRequired(receipt.isReceiptRequested());
            message.setUserEmail(receipt.getUserEmail());
        });
    }

    private static void setCommonMessageData(AbstractAuditMessage auditMsg, Offence offence) {
        auditMsg.setCaseNumber(offence.getCriteria().getCaseNumber());
        auditMsg.setVehicleRegistrationMark(offence.getCriteria().getVehicleRegistrationMark());

        offence.getCaseData().ifPresent(caseData -> {
            CaseData data = offence.getCaseData().get();
            auditMsg.setCaseType(data.getCaseType());
            auditMsg.setArrearsFrom(data.getArrearsInterval().getStart().toDate());
        });
    }

}
