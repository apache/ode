/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.dao;


import java.util.Properties;

public interface BpelDAOConnectionFactory {

  /**
   * Create a JTA transaction-aware state store connection to an the state store.
   * The state store must have previously been created in order for this method to
   * be succesful.
   *
   * @return a {@link BpelDAOConnection} connection object to the state
   *         store.
   */
  public BpelDAOConnection getConnection();
  
  /**
   * Initializes connection factory with properties required to establish a
   * connection.
   *
   * @param properties
   */
  public void init(Properties properties);

}
