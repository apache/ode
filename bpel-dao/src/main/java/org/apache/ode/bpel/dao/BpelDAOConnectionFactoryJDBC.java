package org.apache.ode.bpel.dao;

import javax.sql.DataSource;

/**
 * Extension of the {@link BpelDAOConnectionFactory} interface for DAOs that
 * are based on J2EE/JDBC data sources. 
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 */
public interface BpelDAOConnectionFactoryJDBC extends BpelDAOConnectionFactory {

    /**
     * Set the managed data source (transactions tied to transaction manager). 
     * @param ds
     */
    public void setDataSource(DataSource ds);
    
    /**
     * Set the unmanaged data source.
     * @param ds
     */
    public void setUnmanagedDataSource(DataSource ds);
    
    /**
     * Set the transaction manager.
     * @param tm
     */
    public void setTransactionManager(Object tm);
}
