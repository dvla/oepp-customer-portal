package services;

import configuration.inject.ConfigurationValue;
import play.Logger;
import play.libs.ws.WSClient;
import play.mvc.Http;
import scala.concurrent.duration.Duration;
import services.result.CheckSecurityResult;
import services.result.DeleteSecurityTokensResult;

import javax.inject.Inject;
import java.net.URI;
import java.util.concurrent.CompletionStage;

public class SecurityServiceClient {

    static final String PATH_TO_DELETE_SECURITY_TOKENS = "/security/delete";
    static final String PATH_TO_CHECK_SECURITY = "/security";

    private static final String TOKEN_LIST_PARAM = "tokenList=";
    private static final String TOKEN_LIST_SEPARATOR = "~";
    private static final String KEY_EXPIRY = "keyExpire";
    private static final String MAX_RETRIES = "maxRetries";

    private final URI securityServiceURI;

    @Inject
    @ConfigurationValue(key = "securityService.keyExpiry")
    private String keyExpiry;

    @Inject
    @ConfigurationValue(key = "securityService.maxRetries")
    private String maxRetries;

    @Inject
    private WSClient ws;

    @Inject
    private SecurityServiceClient(@ConfigurationValue(key = "securityService.baseURL") String baseURL) {
        securityServiceURI = URI.create(baseURL);
    }

    public CompletionStage<CheckSecurityResult> checkForBruteForceAttack(Long caseNumber, String vehicleRegistrationMark) {

        String tokenList = getTokenList(caseNumber, vehicleRegistrationMark);
        Long keyExpirySeconds = Duration.create(keyExpiry).toSeconds();

        return ws.url(checkForBruteForceAttackUrl()).setHeader(KEY_EXPIRY, keyExpirySeconds.toString()).setHeader(MAX_RETRIES, maxRetries).post(tokenList).thenApplyAsync(response -> {
            int responseStatus = response.getStatus();
            String responseBody = response.getBody();
            switch (responseStatus) {
                case Http.Status.OK:
                    Logger.debug("Brute force attack check has returned OK for case number {} and VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                    return new CheckSecurityResult.Success();
                case Http.Status.FORBIDDEN:
                    Logger.warn("Brute force attack check has returned forbidden for case number {} and VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                    return new CheckSecurityResult.Forbidden();
                default:
                    Logger.error("Brute force attack check has returned an error for case number {} and VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                    return new CheckSecurityResult.Error();
            }
        }).exceptionally(throwable -> {
            Logger.error("Brute force attack check has thrown {} exception for case number {} and VRM {}", throwable.getClass().getCanonicalName(), caseNumber, vehicleRegistrationMark, throwable);
            return new CheckSecurityResult.Error();
        });
    }

    public CompletionStage<DeleteSecurityTokensResult> deleteTokensUsedToCheckForBruteForceAttack(Long caseNumber, String vehicleRegistrationMark) {

        String tokenList = getTokenList(caseNumber, vehicleRegistrationMark);

        return ws.url(deleteTokensURL()).post(tokenList).thenApplyAsync(response -> {
            int responseStatus = response.getStatus();
            String responseBody = response.getBody();
            switch (responseStatus) {
                case Http.Status.OK:
                    Logger.debug("Brute force token removal has returned OK for case number {} and VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                    return new DeleteSecurityTokensResult.Success();
                default:
                    Logger.error("Brute force token removal has returned an error for case number {} and VRM {} (status: {}, response body: {})", caseNumber, vehicleRegistrationMark, responseStatus, responseBody);
                    return new DeleteSecurityTokensResult.Error();
            }
        }).exceptionally(throwable -> {
            Logger.error("Brute force token removal has thrown {} exception for case number {} and VRM {}", throwable.getClass().getCanonicalName(), caseNumber, vehicleRegistrationMark, throwable);
            return new DeleteSecurityTokensResult.Error();
        });
    }

    private String getTokenList(Long caseNumber, String vehicleRegMark) {
        return TOKEN_LIST_PARAM + caseNumber + TOKEN_LIST_SEPARATOR + vehicleRegMark.toUpperCase();
    }

    private String checkForBruteForceAttackUrl() {
        return securityServiceURI.resolve(PATH_TO_CHECK_SECURITY).toASCIIString();
    }

    private String deleteTokensURL() {
        return securityServiceURI.resolve(PATH_TO_DELETE_SECURITY_TOKENS).toASCIIString();
    }

}
