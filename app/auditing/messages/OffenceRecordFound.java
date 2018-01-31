package auditing.messages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.math.BigDecimal;
import java.util.Date;

@JacksonXmlRootElement(localName = "offenceRecordFound")
public class OffenceRecordFound extends AbstractAuditMessage implements HasOffenceDetails {

    private BigDecimal penaltyAmount;
    private BigDecimal arrearsAmount;
    private BigDecimal voluntaryArrearsAmount;
    private Date arrearsTo;

    @JacksonXmlProperty(isAttribute = true)
    private String caseRejectionCode;

    @JacksonXmlProperty(isAttribute = true)
    private Date caseClosedDate;

    public OffenceRecordFound(String pageTransition) {
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

    public String getCaseRejectionCode() {
        return caseRejectionCode;
    }

    public void setCaseRejectionCode(String caseRejectionCode) {
        this.caseRejectionCode = caseRejectionCode;
    }

    public Date getCaseClosedDate() {
        return caseClosedDate;
    }

    public void setCaseClosedDate(Date caseClosedDate) {
        this.caseClosedDate = caseClosedDate;
    }
}
