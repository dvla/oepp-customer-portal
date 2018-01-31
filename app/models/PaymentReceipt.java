package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.Objects;

public class PaymentReceipt {

    private final boolean receiptRequested;
    private final String userEmail;

    @JsonCreator
    public PaymentReceipt(@JsonProperty("receiptRequested") boolean receiptRequested,
                          @JsonProperty("userEmail") String userEmail) {
        this.receiptRequested = receiptRequested;
        this.userEmail = userEmail;
    }

    public static PaymentReceipt receiptNotRequested() {
        return new PaymentReceipt(false, null);
    }

    public static PaymentReceipt receiptRequestedForEmail(String email) {
        return new PaymentReceipt(true, email);
    }

    public boolean isReceiptRequested() {
        return receiptRequested;
    }

    public String getUserEmail() {
        return userEmail;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaymentReceipt that = (PaymentReceipt) o;
        return receiptRequested == that.receiptRequested &&
                Objects.equals(userEmail, that.userEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiptRequested, userEmail);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("receiptRequested", receiptRequested)
                .add("userEmail", userEmail)
                .toString();
    }
}
