package auditing.messages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;
import java.util.Date;

public interface HasOffenceDetails {

    @JacksonXmlProperty(isAttribute=true)
    BigDecimal getPenaltyAmount();

    void setPenaltyAmount(BigDecimal penaltyAmount);

    @JacksonXmlProperty(isAttribute=true)
    BigDecimal getArrearsAmount();

    void setArrearsAmount(BigDecimal arrearsAmount);

    @JacksonXmlProperty(isAttribute=true, localName = "volArrearsAmount")
    BigDecimal getVoluntaryArrearsAmount();

    void setVoluntaryArrearsAmount(BigDecimal voluntaryArrearsAmount);

    @JacksonXmlProperty(isAttribute=true)
    Date getArrearsTo();

    void setArrearsTo(Date arrearsTo);
}
