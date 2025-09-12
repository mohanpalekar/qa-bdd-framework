package exceptions;

/**
 * Unchecked exception thrown when payload building/overwriting fails.
 */
public class PayloadBuildException extends RuntimeException {
    public PayloadBuildException(String message, Throwable cause) {
        super(message, cause);
    }

    public PayloadBuildException(String message) {
        super(message);
    }
}
