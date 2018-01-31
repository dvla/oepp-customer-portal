package services.result;

public interface CheckSecurityResult {

    class Success implements CheckSecurityResult { }

    class Error implements CheckSecurityResult { }

    class Forbidden implements CheckSecurityResult { }

}
