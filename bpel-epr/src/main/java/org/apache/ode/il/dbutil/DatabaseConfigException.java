package org.apache.ode.il.dbutil;

public class DatabaseConfigException extends Exception {

    private static final long serialVersionUID = 1L;

    public DatabaseConfigException(String msg, Exception ex) {
        super(msg,ex);
    }

}
