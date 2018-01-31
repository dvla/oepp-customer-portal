package services.email;

import com.google.inject.Guice;
import configuration.inject.ConfigurationValue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.WithApplication;
import services.email.model.Email;
import services.email.model.AddressType;

import javax.inject.Inject;

public class EmailServiceClientIT extends WithApplication {

    private final GuiceApplicationBuilder builder;

    @Inject
    @ConfigurationValue(key = "email.feedback.sender")
    private String sender;
    @Inject
    @ConfigurationValue(key = "email.feedback.recipient")
    private String recipient;

    @Inject
    private EmailServiceClient client;

    public EmailServiceClientIT() {
        this.builder = new GuiceApplicationBuilder();
    }

    @Before
    public void init() throws Exception {
        Guice.createInjector(builder.applicationModule()).injectMembers(this);
    }

    @Test
    @Ignore // Remove when connectivity between Jenkins Slave and Amazon SES endpoint is established
    public void testSendEmail() throws Exception {
        client.sendEmail(new Email.Builder()
                .fromSender(sender)
                .toRecipient(AddressType.TO, recipient)
                .withSubject("Sent from " + getClass().getCanonicalName() + " test")
                .withTextBody("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                .create()
        );
    }
}