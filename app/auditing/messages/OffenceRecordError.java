package auditing.messages;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "offenceRecordError")
public class OffenceRecordError extends AbstractAuditMessage {

    public OffenceRecordError(String pageTransition) {
        super(pageTransition);
    }

}
