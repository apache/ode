package org.apache.ode.scheduler.simple;

/**
 * Exception class thrown by {@link DatabaseDelegate} implementations.
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 *
 */
public class DatabaseException extends Exception {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(Exception ex) {
        super(ex);
    }

    public DatabaseException(String message, Exception ex) {
        super(message, ex);
    }
}
