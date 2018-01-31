package services.email;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.*;
import configuration.inject.ConfigurationValue;
import services.email.model.AddressType;
import services.email.model.Email;
import utils.URLParser;

import javax.inject.Inject;
import java.net.URL;
import java.util.Optional;

import static com.amazonaws.regions.RegionUtils.getRegion;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

public class EmailServiceClient {

    private final AmazonSimpleEmailServiceClient client;

    @Inject
    EmailServiceClient(Config config) {
        this(defaultAmazonSimpleEmailServiceClient(config.region, config.credentials, config.client));
    }

    public EmailServiceClient(AmazonSimpleEmailServiceClient client) {
        this.client = client;
    }

    private static AmazonSimpleEmailServiceClient defaultAmazonSimpleEmailServiceClient(String region, Config.Credentials credentialsConfig, Config.Client clientConfig) {
        checkArgument(!isNullOrEmpty(region), "AWS regions is required");
        checkArgument(!isNullOrEmpty(credentialsConfig.accessKeyID), "AWS access key ID is required");
        checkArgument(!isNullOrEmpty(credentialsConfig.secretAccessKey), "AWS secret access key is required");

        return new AmazonSimpleEmailServiceClient(
                new BasicAWSCredentials(credentialsConfig.accessKeyID, credentialsConfig.secretAccessKey),
                newClientConfiguration(clientConfig.connectionTimeout, clientConfig.idleTimeout, clientConfig.requestTimeout, clientConfig.proxy)
        ).withRegion(getRegion(region));
    }

    private static ClientConfiguration newClientConfiguration(Optional<Integer> connectionTimeout, Optional<Integer> idleTimeout, Optional<Integer> requestTimeout, Optional<String> proxy) {
        ClientConfiguration configuration = new ClientConfiguration();

        connectionTimeout.ifPresent(configuration::setConnectionTimeout);
        idleTimeout.ifPresent(configuration::setSocketTimeout);

        requestTimeout.ifPresent(timeout -> {
            configuration.setRequestTimeout(timeout);
            configuration.setClientExecutionTimeout(timeout);
        });

        proxy.ifPresent(proxyConfiguration -> {
            URL proxyURL = URLParser.parse(proxyConfiguration);

            configuration.setProxyHost(proxyURL.getHost());
            configuration.setProxyPort(proxyURL.getPort());
        });

        return configuration;
    }

    public String sendEmail(Email email) {
        try {
            return client.sendEmail(toSendEmailRequest(email)).getMessageId();
        } catch (Exception ex) {
            throw new EmailSendingException(ex);
        }
    }

    private SendEmailRequest toSendEmailRequest(Email email) {
        return new SendEmailRequest()
                .withSource(email.getSender())
                .withDestination(buildDestination(email))
                .withMessage(buildMessage(email));
    }

    private Destination buildDestination(Email email) {
        return new Destination()
                .withToAddresses(email.getRecipients().get(AddressType.TO))
                .withCcAddresses(email.getRecipients().get(AddressType.CC))
                .withBccAddresses(email.getRecipients().get(AddressType.BCC));
    }

    private Message buildMessage(Email email) {
        Message message = new Message()
                .withSubject(new Content(email.getSubject()))
                .withBody(new Body());

        if (email.getTextBody().isPresent()) {
            message.getBody().setText(new Content(email.getTextBody().get()));
        }

        if (email.getHtmlBody().isPresent()) {
            message.getBody().setHtml(new Content(email.getHtmlBody().get()));
        }

        return message;
    }

    private static class Config {

        @Inject
        @ConfigurationValue(key = "aws.ses.region")
        private String region;

        @Inject
        private Credentials credentials;

        @Inject
        private Client client;

        private static class Credentials {

            @Inject
            @ConfigurationValue(key = "aws.ses.accessKeyID")
            private String accessKeyID;
            @Inject
            @ConfigurationValue(key = "aws.ses.secretAccessKey")
            private String secretAccessKey;
        }

        private static class Client {

            @Inject
            @ConfigurationValue(key = "aws.ses.timeout.connection")
            private Optional<Integer> connectionTimeout;
            @Inject
            @ConfigurationValue(key = "aws.ses.timeout.idle")
            private Optional<Integer> idleTimeout;
            @Inject
            @ConfigurationValue(key = "aws.ses.timeout.request")
            private Optional<Integer> requestTimeout;

            @Inject
            @ConfigurationValue(key = "proxy.https.url")
            private Optional<String> proxy;
        }

    }

}
