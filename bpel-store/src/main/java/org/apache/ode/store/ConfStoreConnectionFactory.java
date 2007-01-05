package org.apache.ode.store;

/**
 * Connection factory for DB store. 
 * @author mszefler
  */
public interface ConfStoreConnectionFactory {


    ConfStoreConnection getConnection() ;
}
