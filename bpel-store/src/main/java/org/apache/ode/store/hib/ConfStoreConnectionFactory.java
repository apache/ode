package org.apache.ode.store.hib;

import org.apache.ode.store.ConfStoreConnection;

/**
 * Connection factory for DB store. 
 * @author mszefler
  */
public interface ConfStoreConnectionFactory {

    
    ConfStoreConnection getConnection() ;
}
