package controllers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.junit.Test;
import services.OffenceServiceClient;
import services.SecurityServiceClient;
import services.result.CheckSecurityResult;
import services.result.GetCaseResult;

import java.util.concurrent.ExecutionException;

import static controllers.constants.OffenceConstants.*;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ApplicationControllerWithBruteForceEnabledTest extends BaseStatefulControllerTest {

    private final SecurityServiceClient securityServiceClient;
    private final OffenceServiceClient offenceServiceClient;

    public ApplicationControllerWithBruteForceEnabledTest() {
        securityServiceClient = mock(SecurityServiceClient.class);
        offenceServiceClient = mock(OffenceServiceClient.class);

        builder = builder.configure(ImmutableMap.<String, Object>builder()
                .put("securityService.bruteForceCheckingEnabled", true)
                .build()
        ).overrides(new AbstractModule() {
            @Override
            protected void configure() {
                bind(SecurityServiceClient.class).toInstance(securityServiceClient);
                bind(OffenceServiceClient.class).toInstance(offenceServiceClient);
            }
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToPenaltySummaryPageWhenBruteForceCheckReturnsOK() throws ExecutionException, InterruptedException {
        when(securityServiceClient.checkForBruteForceAttack(CASE_NUMBER, VEHICLE_REGISTRATION_MARK)).thenReturn(completedFuture(new CheckSecurityResult.Success()));
        when(offenceServiceClient.getCase(CASE_NUMBER, VEHICLE_REGISTRATION_MARK, SESSION_ID)).thenReturn(completedFuture(new GetCaseResult.Found(offence())));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ApplicationController.displayPenaltySummaryPage().url()));
            verify(securityServiceClient).deleteTokensUsedToCheckForBruteForceAttack(CASE_NUMBER, VEHICLE_REGISTRATION_MARK);
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToPenaltyDetailsLockedPageWhenBruteForceCheckReturnsForbidden() throws ExecutionException, InterruptedException {
        when(securityServiceClient.checkForBruteForceAttack(CASE_NUMBER, VEHICLE_REGISTRATION_MARK)).thenReturn(completedFuture(new CheckSecurityResult.Forbidden()));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayPenaltyDetailsLockedErrorPage().url()));
            verify(offenceServiceClient, never()).getCase(any(), any(), any());
            verify(securityServiceClient, never()).deleteTokensUsedToCheckForBruteForceAttack(any(), any());
        });
    }

    @Test
    public void onCaseSearch_shouldRedirectToServiceUnavailableErrorPageWhenBruteForceCheckReturnsError() throws ExecutionException, InterruptedException {
        when(securityServiceClient.checkForBruteForceAttack(CASE_NUMBER, VEHICLE_REGISTRATION_MARK)).thenReturn(completedFuture(new CheckSecurityResult.Error()));

        Request request = HTTP.post("/search")
                .withFormParameter("caseNumber", Long.toString(CASE_NUMBER))
                .withFormParameter("vehicleRegistrationMark", VEHICLE_REGISTRATION_MARK);

        makeRequest(request, (result) -> {
            assertThat(result.status(), is(303));
            assertThat(result.redirectLocation().get(), is(routes.ErrorController.displayServiceUnavailableErrorPage().url()));
            verify(offenceServiceClient, never()).getCase(any(), any(), any());
            verify(securityServiceClient, never()).deleteTokensUsedToCheckForBruteForceAttack(any(), any());
        });
    }

}
