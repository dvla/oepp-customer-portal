package controllers;

import org.joda.time.DateTime;
import org.junit.Test;
import uk.gov.dvla.domain.data.CaseRejection;

import java.util.Date;
import java.util.Optional;

import static controllers.constants.OffenceConstants.*;
import static controllers.constants.PaymentConstants.payment;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class ErrorControllerTest extends BaseStatefulControllerTest {

    @Test
    public void caseNotFoundPageShouldExist() {
        Request request = HTTP.get("/error/case-not-found");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void vehicleRegistrationMarkMismatchErrorPageShouldExist() {
        Request request = HTTP.get("/error/vehicle-registration-mark-mismatch");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void paymentAlreadyMadeErrorPageShouldExist() {
        when(session.offence()).thenReturn(Optional.of(offence()));

        Request request = HTTP.get("/error/payment-already-made");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void noPaymentRequiredErrorPageShouldExist() {
        when(session.offence()).thenReturn(Optional.of(offence()));

        Request request = HTTP.get("/error/no-payment-required");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void caseWithADebtRecoveryAgencyErrorPageShouldExist() {
        Request request = HTTP.get("/error/case-with-a-debt-recovery-agency");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void cannotPayOnlineErrorPageShouldExist() {
        Request request = HTTP.get("/error/cannot-pay-online");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void caseWithACourtPageShouldExist() {
        when(session.offence()).thenReturn(Optional.of(offence(CaseRejection.CaseRejectionReason.PASSED_TO_COURT)));

        Request request = HTTP.get("/error/prosecution");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void paymentNotAuthorisedErrorPageShouldExist() {
        when(session.offence()).thenReturn(Optional.of(offence()));
        when(session.payment()).thenReturn(Optional.of(payment()));

        Request request = HTTP.get("/error/payment-not-authorised");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void paymentErrorPageShouldExist() {
        when(session.offence()).thenReturn(Optional.of(offence()));

        Request request = HTTP.get("/error/payment-error");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void serviceUnavailableErrorPageShouldExist() {
        Request request = HTTP.get("/error/service-unavailable");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void penaltyDetailsLockedErrorPageShouldExist() {
        when(session.lockedDate()).thenReturn(Optional.of(DateTime.now().toDate()));

        Request request = HTTP.get("/error/penalty-details-locked");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }

    @Test
    public void pageNotFoundErrorPageShouldExist() {
        Request request = HTTP.get("/error/page-not-found");
        makeRequest(request, (result) -> {
            assertThat(result.status(), is(200));
        });
    }
}
