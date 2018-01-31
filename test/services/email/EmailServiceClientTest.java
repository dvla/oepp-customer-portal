package services.email;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.services.simpleemail.model.SendEmailResult;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import play.test.WithApplication;
import services.email.model.Email;
import services.email.model.AddressType;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailServiceClientTest extends WithApplication {

    private AmazonSimpleEmailServiceClient underlyingClient = mock(AmazonSimpleEmailServiceClient.class);

    private EmailServiceClient client;

    @Before
    public void init() throws Exception {
        client = new EmailServiceClient(underlyingClient);
    }

    @Test
    public void testSendEmail() throws Exception {
        when(underlyingClient.sendEmail(any())).thenReturn(new SendEmailResult().withMessageId("E1"));

        client.sendEmail(new Email.Builder()
                .fromSender("sender@example.com")
                .toRecipient(AddressType.TO, "to@example.com")
                .toRecipient(AddressType.CC, "cc@example.com")
                .toRecipient(AddressType.BCC, "bcc@example.com")
                .withSubject("Some subject")
                .withTextBody("Some message")
                .withHtmlBody("<span>Some message</span>")
                .create()
        );

        ArgumentCaptor<SendEmailRequest> captor = ArgumentCaptor.forClass(SendEmailRequest.class);

        verify(underlyingClient).sendEmail(captor.capture());

        SendEmailRequest capturedValue = captor.getValue();
        assertThat(capturedValue.getSource(), is("sender@example.com"));
        assertThat(capturedValue.getDestination().getToAddresses(), Matchers.contains("to@example.com"));
        assertThat(capturedValue.getDestination().getCcAddresses(), Matchers.contains("cc@example.com"));
        assertThat(capturedValue.getDestination().getBccAddresses(), Matchers.contains("bcc@example.com"));
        assertThat(capturedValue.getMessage().getSubject().getData(), is("Some subject"));
        assertThat(capturedValue.getMessage().getBody().getText().getData(), is("Some message"));
        assertThat(capturedValue.getMessage().getBody().getHtml().getData(), is("<span>Some message</span>"));
    }
}