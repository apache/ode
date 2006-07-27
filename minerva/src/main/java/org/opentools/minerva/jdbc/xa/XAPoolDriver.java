/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.jdbc.xa;

import java.sql.*;
import java.util.Properties;
import javax.sql.*;

/**
 * JDBC Driver to access pooled JDBC connections.  Supports JDBC 2.0
 * transactional XAConnections.  You will get a java.sql.Connection back
 * in any case (the transactional-ness is handled under the covers).  You
 * must create the pools ahead of time by creating and initializing the
 * appropriate DataSource.
 * <P>You should use a URL of the form
 * <B>jdbc:minerva:xa:<I>PoolName</I></B> to get a connection from the
 * pool.</P>
 * <P>This driver will be loaded automatically whenever an XA Pool is
 * created, but you can also use the standard <CODE>Class.forName</CODE>
 * call to load it.</P>
 * @see org.opentools.minerva.jdbc.xa.XAPoolDataSource
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAPoolDriver implements Driver {
    private final static String URL_START = "jdbc:minerva:xa:";
    private final static XAPoolDriver instance;
    static {
        instance = new XAPoolDriver();
        try {
            DriverManager.registerDriver(XAPoolDriver.instance());
        } catch(SQLException e) {
            System.err.println("Unable to register Minerva XA DB pool driver!");
            e.printStackTrace();
        }
    }

    /**
     * Gets the singleton driver instance.
     */
    public static XAPoolDriver instance() {
        return instance;
    }

    private XAPoolDriver() {
    }

    /**
     * Tells which URLs this driver can handle.
     */
    public boolean acceptsURL(String url) throws java.sql.SQLException {
        return url.startsWith(URL_START);
    }

    /**
     * Retrieves a connection from a connection pool.
     */
    public Connection connect(String url, Properties props) throws java.sql.SQLException {
        if(url.startsWith(URL_START)) {
            return getXAConnection(url.substring(URL_START.length()));
        }
        return null;  // No SQL Exception here!
    }

    private Connection getXAConnection(String name) {
        Connection con = null;
        try {
            DataSource source = XAPoolDataSource.getDataSource(name);
            if(source != null)
                con = source.getConnection();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return con;
    }

    /**
     * Returns the driver version.
     */
    public int getMajorVersion() {
        return 2;
    }

    /**
     * Returns the driver version.
     */
    public int getMinorVersion() {
        return 0;
    }

    /**
     * Returns no properties.  You do not need properties to connect to the
     * pool, and the properties for the underlying driver are not managed here.
     */
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    /**
     * Returns <B>false</B> since it is not known which underlying driver will
     * be used and what its capabilities are.
     */
    public boolean jdbcCompliant() {
        return false;
    }
}
