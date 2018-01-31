package services;

import com.fasterxml.jackson.core.type.TypeReference;
import configuration.inject.ConfigurationValue;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import services.result.CloseCaseResult;
import services.result.GetCaseResult;
import uk.gov.dvla.core.error.ErrorResult;
import uk.gov.dvla.domain.Offence;
import uk.gov.dvla.domain.OffenceCloseCommand;
import uk.gov.dvla.domain.data.CaseData;
import uk.gov.dvla.error.OffenceServiceErrors;
import utils.JSON;
import utils.URLParser;

import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletionStage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Throwables.getRootCause;
import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

public class OffenceServiceClient {

    static final String CONVERSATION_ID = "conversationID";

    private final URL offenceServiceURL;

    @Inject
    private WSClient ws;

    @Inject
    public OffenceServiceClient(@ConfigurationValue(key = "offenceService.baseURL") String baseURL) {
        checkArgument(!isNullOrEmpty(baseURL), "Offence service base URL is required");
        try {
            this.offenceServiceURL = URLParser.parse(baseURL);
        } catch (URLParser.ParsingException ex) {
            throw new IllegalArgumentException("Offence service base URL is invalid", ex);
        }
    }

    public CompletionStage<GetCaseResult> getCase(Long caseNumber, String vehicleRegistrationMark, String conversationID) {

        return ws.url(getCaseURL(caseNumber, vehicleRegistrationMark)).setHeader(CONVERSATION_ID, conversationID).get().thenApplyAsync(response -> {

            int responseStatus = response.getStatus();
            String responseBody = response.getBody();
            switch (responseStatus) {
                case Http.Status.OK:
                    Logger.debug("Offence case {} has been found for VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                    return new GetCaseResult.Found(Json.fromJson(response.asJson(), Offence.class));
                case Http.Status.NOT_FOUND:
                    switch (readErrorResult(response).getError()) {
                        case CASE_NOT_FOUND:
                            Logger.debug("Offence case {} doesn't exist (status: {}, response body: {})", caseNumber, responseStatus, responseBody);
                            return new GetCaseResult.NotFound();
                        case VEHICLE_REGISTRATION_MARK_MISMATCH:
                            Logger.debug("Offence case {} VRM doesn't match user entered VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                            return new GetCaseResult.VehicleRegistrationMarkMismatch();
                        case CASE_TYPE_NOT_SUPPORTED:
                            Logger.warn("Case type for offence case {} is not supported (status: {}, response body: {})", caseNumber, responseStatus, responseBody);
                            return new GetCaseResult.NotSupportedCaseType();
                    }
                default:
                    Logger.error("Offence case {} retrieval failed for VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                    return new GetCaseResult.Error();
            }
        }).exceptionally(throwable -> {
            Logger.error("Offence case {} retrieval failed for VRM {} because {} exception has been thrown", caseNumber, vehicleRegistrationMark, throwable.getClass().getCanonicalName(), throwable);
            return new GetCaseResult.Error();
        });
    }

    private String getCaseURL(Long caseNumber, String vehicleRegistrationMark) {
        String path = "/offence/case/" + caseNumber + "/" + deleteWhitespace(vehicleRegistrationMark);
        try {
            return offenceServiceURL.toURI().resolve(path).toASCIIString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public CompletionStage<CloseCaseResult> closeCase(Long paymentID, Long caseNumber, CaseData caseData, String conversationID) {

        OffenceCloseCommand offenceCloseCommand = new OffenceCloseCommand.Builder()
                .setPaidArrearsAmount(caseData.getArrearsAmount())
                .setPayableArrearsAmount(caseData.getPayableArrearsAmount())
                .setPaidPenaltyAmount(caseData.getPenaltyAmount())
                .setVoluntaryArrearsPaid(caseData.getVoluntaryArrearsPaid())
                .setArrearsTo(caseData.getArrearsInterval().getEnd().toDate())
                .setPaymentID(paymentID)
                .create();

        String payload = JSON.stringify(offenceCloseCommand);
        return ws.url(closeCaseURL(caseNumber)).setHeader(CONVERSATION_ID, conversationID).setContentType("application/json").put(payload).thenApplyAsync(response -> {
            int responseStatus = response.getStatus();
            String responseBody = response.getBody();
            switch (responseStatus) {
                case Http.Status.NO_CONTENT:
                    Logger.debug("Offence case {} has been closed with {} payload (status: {}, response body: {})", caseNumber, payload, responseStatus, responseBody);
                    return new CloseCaseResult.Success();
                default:
                    Logger.error("Offence case {} closure failed because unexpected response has been received (status: {}, response body: {}) - reconcile manually {}", caseNumber, responseStatus, responseBody, payload);
                    return new CloseCaseResult.Error();
            }
        }).exceptionally(throwable -> {
            Logger.error("Offence case {} closure failed because {} exception has been thrown - reconcile manually {}", caseNumber, getRootCause(throwable).getClass().getCanonicalName(), payload, throwable);
            return new CloseCaseResult.Error();
        });
    }

    private String closeCaseURL(Long caseNumber) {
        String path = "/offence/case/" + caseNumber + "/close";
        try {
            return offenceServiceURL.toURI().resolve(path).toASCIIString();
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    private ErrorResult<OffenceServiceErrors> readErrorResult(WSResponse response) {
        try {
            return Json.mapper().readValue(response.asByteArray(), new TypeReference<ErrorResult<OffenceServiceErrors>>() {});
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
