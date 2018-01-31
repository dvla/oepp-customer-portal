package services.email.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableMultimap.copyOf;

public class Email {

    private final String sender;
    private final Multimap<AddressType, String> recipients;

    private final String subject;
    private final Optional<String> textBody;
    private final Optional<String> htmlBody;

    public Email(String sender, ImmutableMultimap<AddressType, String> recipients, String subject, Optional<String> textBody, Optional<String> htmlBody) {
        this.sender = sender;
        this.recipients = recipients;
        this.subject = subject;
        this.textBody = textBody;
        this.htmlBody = htmlBody;
    }

    public String getSender() {
        return sender;
    }

    public Multimap<AddressType, String> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public Optional<String> getTextBody() {
        return textBody;
    }

    public Optional<String> getHtmlBody() {
        return htmlBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(sender, email.sender) &&
                Objects.equals(recipients, email.recipients) &&
                Objects.equals(subject, email.subject) &&
                Objects.equals(textBody, email.textBody) &&
                Objects.equals(htmlBody, email.htmlBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender, recipients, subject, textBody, htmlBody);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("sender", sender)
                .add("recipients", recipients)
                .add("subject", subject)
                .add("textBody", textBody)
                .add("htmlBody", htmlBody)
                .toString();
    }

    public static class Builder {

        private String sender;
        private Multimap<AddressType, String> recipients;

        private String subject;
        private Optional<String> textBody;
        private Optional<String> htmlBody;

        public Builder() {
            this.recipients = ArrayListMultimap.create();
            this.textBody = Optional.empty();
            this.htmlBody = Optional.empty();
        }

        public Builder fromSender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder toRecipient(AddressType type, String address) {
            recipients.put(type, address);
            return this;
        }

        public Builder toRecipient(AddressType type, List<String> addresses) {
            recipients.putAll(type, addresses);
            return this;
        }

        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder withTextBody(Optional<String> textBody) {
            this.textBody = textBody;
            return this;
        }

        public Builder withTextBody(String textBody) {
            this.textBody = Optional.ofNullable(textBody);
            return this;
        }

        public Builder withHtmlBody(Optional<String> htmlBody) {
            this.htmlBody = htmlBody;
            return this;
        }

        public Builder withHtmlBody(String htmlBody) {
            this.htmlBody = Optional.ofNullable(htmlBody);
            return this;
        }

        public Email create() {
            return new Email(sender, copyOf(recipients), subject, textBody, htmlBody);
        }

    }

}
