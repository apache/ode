/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi.dao;



/**
 * Factory for {@link com.fs.pxe.sfwk.bapi.dao.DomainStateConnection} objects;
 * these objects are responsible for keeping track of all persisted domain
 * data. The factory pattern is similar to JMS and JDBC connection factories,
 * except that this class also provides some DDL-like functionality including
 * creation, truncation, and removal of stores.
 */
public interface DomainStateConnectionFactory {
  /**
   * Create a deployment store for the given domain.
   *
   * @param domainId domain id
   *
   * @throws com.fs.utils.dao.DConnectionException
   */
  public void createDomainStateStore(String domainId);

  /**
   * Create a data-connection object to the deployment store.
   *
   * @param domainId domain identifier
   *
   * @return a data-connection object
   */
  public DomainStateConnection open(String domainId);

}
