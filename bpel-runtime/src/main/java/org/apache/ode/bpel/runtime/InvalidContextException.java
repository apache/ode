package org.apache.ode.bpel.runtime;

/**
 * Exception used by the runtime to indicate a problem with the execution context. This
 * is for dealing with conditions where the runtime expects certain things of the context
 * and the context does not oblige. These are what one might call "internal errors", for
 * example if a message is received and it does not have the required parts, this execption
 * is thrown, since the runtime expects received messages to be of the correct form.
 *  
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class InvalidContextException extends RuntimeException {

    public InvalidContextException() {
        super();
    }

    public InvalidContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidContextException(String message) {
        super(message);
    }

    public InvalidContextException(Throwable cause) {
        super(cause);
    }

}
