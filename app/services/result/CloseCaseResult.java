package services.result;

public interface CloseCaseResult {

    static class Success implements CloseCaseResult { }

    static class Error implements CloseCaseResult { }

}
