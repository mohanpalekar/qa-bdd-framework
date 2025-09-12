package exceptions;

public class LocatorException extends RuntimeException {

    public LocatorException(String message) {
        super(message);
    }

    public LocatorException(String message, Throwable cause) {
        super(message, cause);
    }
}
