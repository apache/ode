/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi.dao;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;


/**
 * Domain state connection object; permits access to the persisted state of a
 * domain.
 */
public interface DomainStateConnection {

  /**
   * Creates a new deployment system.  This is done once for a system.
   *
   * @param systemID unique ID for the system
   * @param systemName system name
   *
   * @return The new system DAO.
   */
  SystemDAO createSystemDeployment(String systemID, String systemName);

  /**
   * Returns all system DAOs {@link SystemDAO}.
   *
   * @return system DAOs
   */
  Collection<SystemDAO> findAllSystems();

  /**
   * Lookup a system DAO by its uuid.
   *
   * @param systemID system uuid
   *
   * @return the <code>SystemDAO</code>
   */
  SystemDAO findSystem(String systemID);

  /**
   * Find a system deployment record by the name (not the UUID) of the system.
   *
   * @param systemName name to find
   *
   * @return system with the given name or <code>null</code> if none wwas
   *         found
   */
  SystemDAO findDeployedSystemByName(String systemName);

  void close();
}
