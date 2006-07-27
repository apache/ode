/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.connector.jdbc;

import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.security.PasswordCredential;

/**
 * ManagedConnectionFactory implementation for JDBC connections.  You give it
 * a driver, URL, user, and password and it generated connections.  Matches
 * connections based on JDBC user and JDBC URL.  Currently supports managed
 * mode only.
 * <p>To initialize this ResourceAdapter, use the following properties:</p>
 * <table>
 *   <tr><th align="right">Driver</th><td>The driver class name</td></tr>
 *   <tr><th align="right">ConnectionUrl</th><td>The JDBC URL</td></tr>
 *   <tr><th align="right">UserName</th><td>The default JDBC UserName</td></tr>
 *   <tr><th align="right">Password</th><td>The default JDBC Password</td></tr>
 * </table>
 *
 * @author Aaron Mulder <ammulder@alumni.princeton.edu>
 * @version $Revision: 4 $
 */
public class JDBCManagedConnectionFactory implements ManagedConnectionFactory {
    private String driver;
    private String url;
    private String username;
    private String password;
    private transient PrintWriter logger;

    public JDBCManagedConnectionFactory() {
    }

    public Object createConnectionFactory(ConnectionManager mgr) throws ResourceException {
        return new JDBCDataSource(mgr, this);
    }
    public Object createConnectionFactory() throws ResourceException {
        throw new java.lang.UnsupportedOperationException("Must be used in managed mode");
    }
    public ManagedConnection createManagedConnection(Subject sub, ConnectionRequestInfo info) throws ResourceException {
        // Set user and password to default
        String user = username;
        String pw = password;

        // Check passed Subject and ConnectionRequestInfo for user/password overrides
        if(sub != null) {
            Set creds = sub.getPrivateCredentials(javax.resource.spi.security.PasswordCredential.class);
            for(Iterator it = creds.iterator(); it.hasNext(); ) {
                PasswordCredential pc = (PasswordCredential)it.next();
                user = pc.getUserName();
                pw = new String(pc.getPassword());
                break;
            }
        } else {
            if(info != null) {
                JDBCConnectionRequestInfo jdbcInfo = (JDBCConnectionRequestInfo)info;
                user = jdbcInfo.user;
                pw = jdbcInfo.password;
            }
        }

        // Create the connection
        try {
            Connection con = DriverManager.getConnection(url, user, pw);
            con.setAutoCommit(false);
            ManagedConnection mc = new JDBCManagedConnection(con, user, url);
            mc.setLogWriter(logger);
            return mc;
        } catch(SQLException e) {
            throw new ResourceException("Unable to create DB connection: "+e);
        }
    }

    public ManagedConnection matchManagedConnections(Set cons, Subject sub, ConnectionRequestInfo info) throws ResourceException {
        // Set user and password to default
        String user = username;
        String pw = password;

        // Check passed Subject and ConnectionRequestInfo for user/password overrides
        if(sub != null) {
            Set creds = sub.getPrivateCredentials(javax.resource.spi.security.PasswordCredential.class);
            for(Iterator it = creds.iterator(); it.hasNext(); ) {
                PasswordCredential pc = (PasswordCredential)it.next();
                user = pc.getUserName();
                pw = new String(pc.getPassword());
                break;
            }
        } else {
            if(info != null) {
                if(!(info instanceof JDBCConnectionRequestInfo)) {
                    throw new ResourceException("Passed ConnectionRequestInfo class '"+info.getClass().getName()+"' to JDBCManagedConnectionFactory!");
                }
                JDBCConnectionRequestInfo jdbcInfo = (JDBCConnectionRequestInfo)info;
                user = jdbcInfo.user;
                pw = jdbcInfo.password;
            }
        }

        // Check the connections in the Set
        for(Iterator it = cons.iterator(); it.hasNext(); ) {
            Object unknown = it.next();
            if(!(unknown instanceof JDBCManagedConnection)) {
                continue;
            }
            JDBCManagedConnection con = (JDBCManagedConnection)unknown;
            if(con.getUser().equals(user) && con.getURL().equals(url)) {
                return con;
            }
        }
        return null;
    }

    public void setLogWriter(PrintWriter writer) throws ResourceException {
        logger = writer;
    }
    public PrintWriter getLogWriter() throws ResourceException {
        return logger;
    }

    public void setDriver(String driver) {
        this.driver = driver;
        try {
            Class.forName(driver);
        } catch(Exception e) {
            if(logger != null) {
                logger.println("Unable to load JDBC driver '"+driver+"'");
                e.printStackTrace(logger);
            } else {
                System.err.println("Unable to load JDBC driver '"+driver+"'");
                e.printStackTrace();
            }
        }
    }
    public String getDriver() {return driver;}

    public void setConnectionURL(String url) {this.url = url;}
    public String getConnectionURL() {return url;}

    public void setUserName(String username) {this.username = username;}
    public String getUserName() {return username;}

    public void setPassword(String password) {this.password = password;}
    public String getPassword() {return password;}
}
