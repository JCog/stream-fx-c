package utilities;

public class StartupException extends RuntimeException {
    public StartupException(String message) {
        super("StartupException: " + message);
    }
}
