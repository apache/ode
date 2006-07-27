/*
 * Licensed under the X license (see http://www.x.org/terms.htm)
 */
package org.opentools.minerva.jdbc.xa;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.*;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.opentools.minerva.pool.ObjectPool;

/**
 * DataSource for transactional JDBC pools.  This handles configuration
 * parameters for both the pool and the JDBC connection.  It is important that
 * you set all the configuration parameters before you initialize the DataSource,
 * and you must initialize it before you use it.  All the configuration
 * parameters are not documented here; you are instead referred to ObjectPool
 * and XAConnectionFactory.
 * @see org.opentools.minerva.pools.ObjectPool
 * @see org.opentools.minerva.factories.XAConnectionFactory
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class XAPoolDataSource implements DataSource, Referenceable, ObjectFactory {
    private static Map<String, DataSource> sources = new HashMap<String, DataSource>();

    /**
     * Gets all the current JDBC pool data sources.
     */
    public static Collection<DataSource> getDataSources() {
        return new HashSet<DataSource>(sources.values());
    }
    /**
     * Gets a specific JDBC pool data source by pool name.
     */
    public static XAPoolDataSource getDataSource(String poolName) {
        return (XAPoolDataSource)sources.get(poolName);
    }


    private ObjectPool pool;
    private XAConnectionFactory factory;
    private PrintWriter logWriter;
    private int timeout;
    private boolean initialized = false;
    private String jndiName;

    /**
     * Creates a new XA pool data source.  Be sure to configure it and then
     * call initialize before you try to use it.
     */
    public XAPoolDataSource() {
        pool = new ObjectPool();
        try {
            factory = new XAConnectionFactory();
        } catch(NamingException e) {
            e.printStackTrace();
        }
        XAPoolDriver.instance();
    }

// Unique properties
    /**
     * If you use this to set a JNDI name, this pool will be bound to that name
     * using the default InitialContext.  You can also do this manually if you
     * have additional requirements.
     */
    public void setJNDIName(String name) throws NamingException {
        InitialContext ctx = new InitialContext();
        if(jndiName != null && !jndiName.equals(name))
            ctx.unbind(jndiName);
        if(name != null)
            ctx.bind(name, this);
        jndiName = name;
    }

    /**
     * Gets the JNDI name this pool is bound to.  Only valid if you used
     * setJNDIName to bind it.
     * @see #setJNDIName
     */
    public String getJNDIName() {return jndiName;}

    // XA properties
    public void setDataSource(XADataSource ds) {factory.setDataSource(ds);}
    public XADataSource getDataSource() {return factory.getDataSource();}
    public void setTransactionManagerJNDIName(String name) {factory.setTransactionManagerJNDIName(name);}
    public String getTransactionManagerJNDIName() {return factory.getTransactionManagerJNDIName();}
    public void setJDBCUser(String user) {factory.setUser(user);}
    public String getJDBCUser() {return factory.getUser();}
    public void setJDBCPassword(String password) {factory.setPassword(password);}
    public String getJDBCPassword() {return factory.getPassword();}
    // Pool properties
    public void setPoolName(String name) {
        pool.setName(name);
        sources.put(pool.getName(), this);
    }
    public String getPoolName() {return pool.getName();}
    public void setMinSize(int size) {pool.setMinSize(size);}
    public int getMinSize() {return pool.getMinSize();}
    public void setMaxSize(int size) {pool.setMaxSize(size);}
    public int getMaxSize() {return pool.getMaxSize();}
    public void setBlocking(boolean blocking) {pool.setBlocking(blocking);}
    public boolean isBlocking() {return pool.isBlocking();}
    public void setIdleTimeoutEnabled(boolean allowShrinking) {pool.setIdleTimeoutEnabled(allowShrinking);}
    public boolean isIdleTimeoutEnabled() {return pool.isIdleTimeoutEnabled();}
    public void setGCEnabled(boolean allowGC) {pool.setGCEnabled(allowGC);}
    public boolean isGCEnabled() {return pool.isGCEnabled();}
    public void setMaxIdleTimeoutPercent(float percent) {pool.setMaxIdleTimeoutPercent(percent);}
    public float getMaxIdleTimeoutPercent() {return pool.getMaxIdleTimeoutPercent();}
    public void setIdleTimeout(long millis) {pool.setIdleTimeout(millis);}
    public long getIdleTimeout() {return pool.getIdleTimeout();}
    public void setGCMinIdleTime(long millis) {pool.setGCMinIdleTime(millis);}
    public long getGCMinIdleTime() {return pool.getGCMinIdleTime();}
    public void setGCInterval(long millis) {pool.setGCInterval(millis);}
    public long getGCInterval() {return pool.getGCInterval();}
    public void setInvalidateOnError(boolean invalidate) {pool.setInvalidateOnError(invalidate);}
    public boolean isInvalidateOnError() {return pool.isInvalidateOnError();}
    public void setTimestampUsed(boolean timestamp) {pool.setTimestampUsed(timestamp);}
    public boolean isTimestampUsed() {return pool.isTimestampUsed();}

// Other methods

    /**
     * Initializes the pool.  You need to have configured all the pool and
     * XA properties first.
     */
    public void initialize() {
        initialized = true;
        pool.setObjectFactory(factory);
        pool.initialize();
    }

    /**
     * Returns a string describing the pool status (number of connections
     * created, used, and maximum).
     */
    public String getPoolStatus() {
        return pool.toString();
    }

    /**
     * Shuts down this data source and the underlying pool.  If you used
     * setJNDI name to bind it in JNDI, it is unbound.
     */
    public void close() {
        try {
            setJNDIName(null);
        } catch(NamingException e) {
            if(logWriter != null)
                e.printStackTrace(logWriter);
        }
        sources.remove(pool.getName());
        pool.shutDown();
        pool = null;
        factory = null;
    }

    /**
     * Gets a connection from the pool.
     */
    public Connection getConnection() throws java.sql.SQLException {
        if(!initialized) initialize();
        return ((XAConnection)pool.getObject()).getConnection();
    }

    /**
     * Gets a new connection from the pool.  If a new connection must be
     * created, it will use the specified user name and password.  If there is
     * a connection available in the pool, it will be used, regardless of the
     * user name and password use to created it initially.
     */
    public Connection getConnection(String user, String password) throws java.sql.SQLException {
        if(!initialized) initialize();
        factory.setUser(user);
        factory.setPassword(password);
        return ((XAConnection)pool.getObject()).getConnection();
    }

    /**
     * Gets a log writer used to record pool events.
     */
    public PrintWriter getLogWriter() throws java.sql.SQLException {
        return logWriter;
    }

    /**
     * Sets a log writer used to record pool events.
     */
    public void setLogWriter(PrintWriter writer) throws java.sql.SQLException {
        logWriter = writer;
        pool.setLogWriter(writer);
    }

    /**
     * This property is not used by this implementation.
     */
    public int getLoginTimeout() throws java.sql.SQLException {
        return timeout;
    }

    /**
     * This property is not used by this implementation.
     */
    public void setLoginTimeout(int timeout) throws java.sql.SQLException {
        this.timeout = timeout;
    }

    // Referenceable implementation ----------------------------------
    /**
     * Gets a reference to this data source.
     */
    public Reference getReference() {
        return new Reference(getClass().getName(), new StringRefAddr("XAPool", pool.getName()), getClass().getName(), null);
    }

    // ObjectFactory implementation ----------------------------------
    /**
     * Decodes a reference to a specific pool data source.
     */
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment) {
        if(obj instanceof Reference) {
            Reference ref = (Reference)obj;
            if(ref.getClassName().equals(getClass().getName())) {
                RefAddr addr = ref.get("XAPool");
                return sources.get(addr.getContent());
            }
        }
        return null;
    }
}
