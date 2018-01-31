package models.form;

import com.google.common.base.MoreObjects;
import models.form.validation.groups.EmailChecks;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.dvla.validation.constraints.ConditionalValidation;
import uk.gov.dvla.validation.constraints.PropertiesMatch;
import uk.gov.dvla.validation.constraints.PropertiesMatchValidator.CaseInsensitiveStringComparator;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@ConditionalValidation(dependsOn = "emailReceiptDecision", revalidate = "receiptEmails", withGroups = EmailChecks.class)
public class PaymentReceiptModel {

    @NotNull(message = "error.required.emailReceiptDecision")
    private Boolean emailReceiptDecision;
    @Valid
    private Emails receiptEmails;

    public PaymentReceiptModel() {}

    public PaymentReceiptModel(Boolean emailReceiptDecision, String receiptEmail) {
        this(emailReceiptDecision, new Emails(receiptEmail));
    }

    public PaymentReceiptModel(Boolean emailReceiptDecision, Emails receiptEmails) {
        this.emailReceiptDecision = emailReceiptDecision;
        this.receiptEmails = receiptEmails;
    }

    public Boolean getEmailReceiptDecision() {
        return emailReceiptDecision;
    }

    public void setEmailReceiptDecision(Boolean emailReceiptDecision) {
        this.emailReceiptDecision = emailReceiptDecision;
    }

    public Emails getReceiptEmails() {
        return receiptEmails;
    }

    public void setReceiptEmails(Emails receiptEmails) {
        this.receiptEmails = receiptEmails;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("emailReceiptDecision", emailReceiptDecision)
                .add("receiptEmails", receiptEmails)
                .toString();
    }

    @PropertiesMatch(properties = {"email", "repeatedEmail"}, violationOn = {"repeatedEmail"}, comparator = CaseInsensitiveStringComparator.class, message = "error.mismatch.repeatedEmail", groups = EmailChecks.class)
    public static class Emails {

        @NotBlank(message = "error.required.email", groups = EmailChecks.class)
        @Email(message = "error.invalid.email", groups = EmailChecks.class)
        private String email;

        @NotBlank(message = "error.required.repeatedEmail", groups = EmailChecks.class)
        @Email(message = "error.invalid.repeatedEmail", groups = EmailChecks.class)
        private String repeatedEmail;

        public Emails() {}

        public Emails(String value) {
            this.email = value;
            this.repeatedEmail = value;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRepeatedEmail() {
            return repeatedEmail;
        }

        public void setRepeatedEmail(String repeatedEmail) {
            this.repeatedEmail = repeatedEmail;
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("email", email)
                    .add("repeatedEmail", repeatedEmail)
                    .toString();
        }
    }
}