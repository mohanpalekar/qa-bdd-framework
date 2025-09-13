package exceptions;
/**
 * Thrown when a web element cannot be located or is not usable.
 */
public class ElementNotFoundException extends RuntimeException {
    public ElementNotFoundException(String message) {
        super(message);
    }

    @SuppressWarnings("unused")
    public ElementNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
