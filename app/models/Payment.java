package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

public class Payment {

    private final Long id;
    private final URL formURL;
    private Optional<Date> paidDate;

    @JsonCreator
    public Payment(@JsonProperty("id") Long id,
                   @JsonProperty("formURL") URL formURL) {
        this.id = id;
        this.formURL = formURL;
    }

    public Long getId() {
        return id;
    }

    public URL getFormURL() {
        return formURL;
    }

    public Optional<Date> getPaidDate() {
        return paidDate;
    }

    public Payment setPaidDate(Optional<Date> paidDate) {
        this.paidDate = paidDate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return Objects.equals(id, payment.id) &&
                Objects.equals(formURL, payment.formURL) &&
                Objects.equals(paidDate, payment.paidDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, formURL, paidDate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("formURL", formURL)
                .add("paidDate", paidDate)
                .toString();
    }
}
