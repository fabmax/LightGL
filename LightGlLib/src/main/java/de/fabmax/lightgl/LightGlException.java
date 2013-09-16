package de.fabmax.lightgl;

/**
 * A LightGlException is thrown on occurrence of OpenGL related errors.
 * 
 * @author fabmax
 * 
 */
public class LightGlException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new LightGlException with the specified message.
     * 
     * @param message
     *            the error message
     */
    public LightGlException(String message) {
        super(message);
    }

    /**
     * Constructs a new LightGlException with the specified message and cause.
     * 
     * @param message
     *            the error message
     * @param cause
     *            the exception that caused this LightGlException
     */
    public LightGlException(String message, Throwable cause) {
        super(message, cause);
    }
}
