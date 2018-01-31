package services.result;

public interface DeleteSecurityTokensResult {

    class Success implements DeleteSecurityTokensResult { }

    class Error implements DeleteSecurityTokensResult { }

}
