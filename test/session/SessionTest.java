package session;

import com.google.common.collect.ImmutableMap;
import models.Payment;
import models.PaymentReceipt;
import org.junit.Before;
import org.junit.Test;
import play.Application;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.domain.data.CaseData;
import uk.gov.dvla.domain.data.VehicleData;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static play.test.Helpers.fakeRequest;
import static utils.URLParser.parse;

public class SessionTest extends WithApplication {

    private static final String SESSION_ID = "my-session-id";

    private SessionManager.Session session;

    @Override
    protected Application provideApplication() {
        return Helpers.fakeApplication(ImmutableMap.<String, Object>builder().build());
    }

    @Before
    public void init() {
        setContext(fakeRequest().session("id", SESSION_ID));
        this.session = app.injector().instanceOf(SessionManager.class).session();
        this.session.clear();
    }

    @Test
    public void offence_shouldReturnEmptyOptionalWhenValueDoesNotExist() {
        assertThat(session.offence().isPresent(), is(false));
    }

    @Test
    public void offence_shouldReturnNonEmptyOptionalWhenValueExists() {
        Offence offence = fullyPopulatedOffence();

        session.setOffence(offence);

        assertThat(session.offence().isPresent(), is(true));
        assertThat(session.offence().get(), is(offence));
    }

    private Offence fullyPopulatedOffence() {
        return new Offence.Builder()
                .setCriteria(new Offence.Criteria(1L, "CA57ABC"))
                .setCaseData(new CaseData.Builder()
                        .setPenaltyAmount(BigDecimal.ONE)
                        .setElevatedPenaltyAmount(Optional.empty())
                        .setArrearsAmount(BigDecimal.ZERO)
                        .create()
                ).setVehicleData(new VehicleData.Builder()
                        .setTaxed(true)
                        .setNewTaxStartDate(Optional.empty())
                        .create()
                ).create();
    }

    @Test
    public void offence_shouldRemoveValueWhenRequested() {
        session.setOffence(new Offence.Builder().create());
        session.removeOffence();

        assertThat(session.offence().isPresent(), is(false));
    }

    @Test
    public void payment_shouldReturnEmptyOptionalWhenValueDoesNotExist() {
        assertThat(session.payment().isPresent(), is(false));
    }

    @Test
    public void payment_shouldReturnNonEmptyOptionalWhenValueExists() {
        Payment payment = fullyPopulatedPayment();

        session.setPayment(payment);

        assertThat(session.payment().isPresent(), is(true));
        assertThat(session.payment().get(), is(payment));
    }

    private Payment fullyPopulatedPayment() {
        return new Payment(123L, parse("http://localhost"))
                .setPaidDate(Optional.of(new Date()));
    }

    @Test
    public void payment_shouldRemoveValueWhenRequested() {
        session.setPayment(new Payment(null, null));
        session.removePayment();

        assertThat(session.payment().isPresent(), is(false));
    }

    @Test
    public void paymentReceipt_shouldReturnEmptyOptionalWhenValueDoesNotExist() {
        assertThat(session.paymentReceipt().isPresent(), is(false));
    }

    @Test
    public void paymentReceipt_shouldReturnNonEmptyOptionalWhenValueExists() {
        PaymentReceipt paymentReceipt = fullyPopulatedPaymentReceipt();

        session.setPaymentReceipt(paymentReceipt);

        assertThat(session.paymentReceipt().isPresent(), is(true));
        assertThat(session.paymentReceipt().get(), is(paymentReceipt));
    }

    private PaymentReceipt fullyPopulatedPaymentReceipt() {
        return new PaymentReceipt(true, "user@example.com");
    }

    @Test
    public void paymentReceipt_shouldRemoveValueWhenRequested() {
        session.setPaymentReceipt(new PaymentReceipt(false, null));
        session.removePaymentReceipt();

        assertThat(session.paymentReceipt().isPresent(), is(false));
    }

    @Test
    public void clear_shouldRemoveEverything() {
        session.setOffence(new Offence.Builder().create());
        session.setPayment(new Payment(null, null));
        session.setPaymentReceipt(new PaymentReceipt(false, null));

        session.clear();

        assertThat(session.offence().isPresent(), is(false));
        assertThat(session.payment().isPresent(), is(false));
        assertThat(session.paymentReceipt().isPresent(), is(false));
    }

    private void setContext(Http.RequestBuilder builder) {
        Http.Context.current.set(new Http.Context(builder));
    }

}