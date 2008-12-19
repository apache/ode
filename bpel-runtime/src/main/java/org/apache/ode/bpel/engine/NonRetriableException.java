package org.apache.ode.bpel.engine;

public class NonRetriableException extends RuntimeException {
	private static final long serialVersionUID = 554086183512998349L;

	public NonRetriableException() {
        super();
    }

    public NonRetriableException(String string) {
        this(string, null);
    }

    public NonRetriableException(String string, Throwable ex) {
        super(string, ex);
    }
}
