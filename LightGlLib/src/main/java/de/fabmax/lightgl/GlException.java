package de.fabmax.lightgl;

/**
 * A GlException is thrown on occurrence of OpenGL related errors.
 * 
 * @author fabmax
 * 
 */
public class GlException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new GlException with the specified message.
     * 
     * @param message
     *            the error message
     */
    public GlException(String message) {
        super(message);
    }

    /**
     * Constructs a new GlException with the specified message and cause.
     * 
     * @param message
     *            the error message
     * @param cause
     *            the exception that caused this GlException
     */
    public GlException(String message, Throwable cause) {
        super(message, cause);
    }
}
