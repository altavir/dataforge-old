package hep.dataforge.exceptions;

/**
 * An exception to be thrown when automatic class cast is failed
 */
public class AutoCastException extends RuntimeException {
    private final Class from;
    private final Class to;

    public AutoCastException(String message, Class from, Class to, Throwable cause) {
        super(message, cause);
        this.from = from;
        this.to = to;
    }

    public AutoCastException(String message, Class from, Class to) {
        super(message);
        this.from = from;
        this.to = to;
    }

    public AutoCastException(Class from, Class to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String getMessage() {
        return String.format("Automatic class cast from %s to %s failed", from, to) + super.getMessage();
    }
}
