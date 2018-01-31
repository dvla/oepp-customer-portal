package logging;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import play.test.Helpers;
import session.SessionManager;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.domain.data.VehicleData;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MDCInterceptorTest {

    private MDCInterceptor.ContextHelper contextHelper;

    @Before
    public void init() {
        MDC.clear();
        contextHelper = new MDCInterceptor().new ContextHelper();
    }

    @Test
    public void initiate_shouldTakeValuesFromSessionWhenOffenceExistsInSession() {
        Offence offence = new Offence.Builder()
                .setCriteria(new Offence.Criteria(11110002L, "CV02AAA"))
                .setVehicleData(new VehicleData.Builder().setTaxed(true).create())
                .create();

        SessionManager.Session session = mock(SessionManager.Session.class);
        when(session.offence()).thenReturn(Optional.of(offence));

        contextHelper.initiate(Helpers.fakeRequest().build(), Optional.of(session));

        assertThat(MDC.get("PRN"), is("11110002"));
        assertThat(MDC.get("VRM"), is("CV02AAA"));
    }

    @Test
    public void initiate_shouldTakeValuesFromRequestWhenOffenceDoesNotExistInSession() {
        contextHelper.initiate(Helpers.fakeRequest()
                .bodyFormArrayValues(ImmutableMap.<String, String[]>builder()
                        .put("caseNumber", new String[]{"11110003"})
                        .put("vehicleRegistrationMark", new String[]{"CV03AAA"})
                        .build())
                .build(), Optional.empty()
        );

        assertThat(MDC.get("PRN"), is("11110003"));
        assertThat(MDC.get("VRM"), is("CV03AAA"));
    }

    @Test
    public void initiate_shouldTakeDefaultValueWhenValueExtractingFailed() {
        SessionManager.Session session = mock(SessionManager.Session.class);
        when(session.offence()).thenReturn(Optional.empty());

        contextHelper.initiate(Helpers.fakeRequest().build(), Optional.empty());

        assertThat(MDC.get("PRN"), is("-"));
        assertThat(MDC.get("VRM"), is("-"));
    }

    @Test
    public void clear_shouldRemoveAllValuesFromContext() {
        MDC.put("PRN", "11110001");
        MDC.put("VRM", "CV01AAA");

        contextHelper.clear();

        assertThat(MDC.get("PRN"), is(nullValue()));
        assertThat(MDC.get("VRM"), is(nullValue()));
    }

}