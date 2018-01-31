package auditing.messages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import uk.gov.dvla.oepp.audit.service.message.AuditMessage;

import java.util.Date;

public abstract class AbstractAuditMessage implements AuditMessage {

    @JacksonXmlProperty(isAttribute=true)
    private final String serviceType = "OEPP";

    @JacksonXmlProperty(isAttribute=true, localName = "pageMovement")
    private final String pageTransition;

    @JacksonXmlProperty(isAttribute=true, localName = "offenceType")
    private String caseType;

    @JacksonXmlProperty(isAttribute=true, localName = "penaltyReferenceNumber")
    private Long caseNumber;

    @JacksonXmlProperty(isAttribute=true, localName = "VRM")
    private String vehicleRegistrationMark;

    @JacksonXmlProperty(isAttribute=true, localName = "DOL")
    private Date arrearsFrom;

    @JacksonXmlProperty(isAttribute=true, localName = "timeStamp")
    private Date dateTimeStamp;

    public AbstractAuditMessage(String pageTransition) {
        this.pageTransition = pageTransition;
        this.dateTimeStamp = new Date();
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getPageTransition() {
        return pageTransition;
    }

    public String getCaseType() {
        return caseType;
    }

    public void setCaseType(String caseType) {
        this.caseType = caseType;
    }

    public Long getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(Long caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getVehicleRegistrationMark() {
        return vehicleRegistrationMark;
    }

    public void setVehicleRegistrationMark(String vehicleRegistrationMark) {
        this.vehicleRegistrationMark = vehicleRegistrationMark;
    }

    public Date getArrearsFrom() {
        return arrearsFrom;
    }

    public void setArrearsFrom(Date arrearsFrom) {
        this.arrearsFrom = arrearsFrom;
    }

    public Date getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(Date dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }
}
