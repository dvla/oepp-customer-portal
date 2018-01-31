package auditing.messages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public interface HasReceiptDetails {

    @JacksonXmlProperty(isAttribute=true)
    boolean getReceiptRequired();

    void setReceiptRequired(boolean receiptRequired);

    @JacksonXmlProperty(isAttribute=true)
    String getUserEmail();

    void setUserEmail(String userEmail);
}
