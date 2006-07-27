/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.jdbc;

import java.sql.*;
import java.util.Properties;
import javax.sql.*;

/**
 * JDBC Driver to access pooled JDBC connections.  Supports both JDBC 1.0
 * connections and JDBC 2.0 transactional XAConnections.  You will get a
 * java.sql.Connection back in any case (in the case of XAConnections, the
 * transactional-ness is handled under the covers).  You must create the pools
 * ahead of time by creating and initializing the appropriate DataSource.
 * <P>You should use a URL of the form <B>jdbc:minerva:<I>PoolName</I></B>
 * to get a connection from the pool.  This will check for both a JDBC Pool
 * and XA Pool if necessary, so don't create one pool of each type with the
 * same name!</P>
 * <P>This driver will be loaded automatically whenever a JDBC Pool or XA
 * Pool is created, but you can also use the standard <CODE>Class.forName</CODE>
 * call to load it.</P>
 * @see org.opentools.minerva.jdbc.JDBCPoolDataSource
 * @see org.opentools.minerva.jdbc.xa.XAPoolDataSource

 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class PoolDriver implements Driver {
    private final static String URL_START = "jdbc:minerva:";
    private final static PoolDriver instance;
    static {
        instance = new PoolDriver();
        try {
            DriverManager.registerDriver(PoolDriver.instance());
        } catch(SQLException e) {
            System.out.println("Unable to register Minerva DB pool driver!");
            e.printStackTrace();
        }
    }

    /**
     * Gets the singleton driver instance.
     */
    public static PoolDriver instance() {
        return instance;
    }

    private PoolDriver() {
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
            return getJDBCConnection(url.substring(URL_START.length()));
        }
        return null;  // No SQL Exception here!
    }

    private Connection getJDBCConnection(String name) {
        Connection con = null;
        try {
            DataSource source = JDBCPoolDataSource.getDataSource(name);
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
