package auditing.messages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import java.util.Date;

@JacksonXmlRootElement(localName = "paymentInitiated")
public class PaymentInitiated extends AbstractAuditMessage implements HasOffenceDetails, HasReceiptDetails {

    private BigDecimal penaltyAmount;
    private BigDecimal arrearsAmount;
    private BigDecimal voluntaryArrearsAmount;
    private Date arrearsTo;
    private boolean receiptRequired;
    private String userEmail;

    public PaymentInitiated(String pageTransition) {
        super(pageTransition);
    }

    @Override
    public BigDecimal getPenaltyAmount() {
        return penaltyAmount;
    }

    @Override
    public void setPenaltyAmount(BigDecimal penaltyAmount) {
        this.penaltyAmount = penaltyAmount;
    }

    @Override
    public BigDecimal getArrearsAmount() {
        return arrearsAmount;
    }

    @Override
    public void setArrearsAmount(BigDecimal arrearsAmount) {
        this.arrearsAmount = arrearsAmount;
    }

    @Override
    public BigDecimal getVoluntaryArrearsAmount() {
        return voluntaryArrearsAmount;
    }

    @Override
    public void setVoluntaryArrearsAmount(BigDecimal voluntaryArrearsAmount) {
        this.voluntaryArrearsAmount = voluntaryArrearsAmount;
    }

    @Override
    public Date getArrearsTo() {
        return arrearsTo;
    }

    @Override
    public void setArrearsTo(Date arrearsTo) {
        this.arrearsTo = arrearsTo;
    }

    @Override
    public boolean getReceiptRequired() {
        return receiptRequired;
    }

    @Override
    public void setReceiptRequired(boolean receiptRequired) {
        this.receiptRequired = receiptRequired;
    }

    @Override
    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
