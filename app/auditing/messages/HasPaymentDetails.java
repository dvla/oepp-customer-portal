package auditing.messages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.math.BigDecimal;

public interface HasPaymentDetails {

    @JacksonXmlProperty(isAttribute=true)
    Long getPaymentId();

    void setPaymentId(Long paymentId);

    @JacksonXmlProperty(isAttribute=true)
    BigDecimal getTotalAmountPaid();

    void setTotalAmountPaid(BigDecimal totalAmountPaid);

}
