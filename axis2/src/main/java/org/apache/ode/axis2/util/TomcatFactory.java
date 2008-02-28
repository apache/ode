package org.apache.ode.axis2.util;

import javax.transaction.TransactionManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class TomcatFactory {

    public static final String JNDI_LOOKUP_PATH = "java:comp/UserTransaction";

    /* Public no-arg contructor is required */
    public TomcatFactory() {
    }

    public TransactionManager getTransactionManager() {
        InitialContext ctx;
        try {
           ctx = new InitialContext();
        } catch (NamingException except) {
            throw new RuntimeException( "Can't create InitialContext", except);
        }
        try {
            return (TransactionManager) ctx.lookup(JNDI_LOOKUP_PATH);
        } catch (NamingException except) {
            throw new RuntimeException( "Error while looking up TransactionManager at " + JNDI_LOOKUP_PATH, except);
        }
    }

}
package org.apache.ode.axis2.util;

import javax.transaction.TransactionManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class TomcatFactory {

    public static final String JNDI_LOOKUP_PATH = "java:comp/UserTransaction";

    /* Public no-arg contructor is required */
    public TomcatFactory() {
    }

    public TransactionManager getTransactionManager() {
        InitialContext ctx;
        try {
           ctx = new InitialContext();
        } catch (NamingException except) {
            throw new RuntimeException( "Can't create InitialContext", except);
        }
        try {
            return (TransactionManager) ctx.lookup(JNDI_LOOKUP_PATH);
        } catch (NamingException except) {
            throw new RuntimeException( "Error while looking up TransactionManager at " + JNDI_LOOKUP_PATH, except);
        }
    }

}
