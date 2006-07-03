/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.bapi.dao;

import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Iterator;


/**
 * Maintains long-term persistent state of the system and its message
 * exchanges.
 */
public interface SystemDAO {

  /**
   * Set the deployed state for the system.
   * @param deployed whether the system should be deployed
   */
  void setDeployed(boolean deployed);

  /**
   * Get the <em>deployed</em> flag for this system.
   * @return
   */
  boolean isDeployed();


  /**
   * Set the <em>enabled</em> flag for this system; this flag indicates
   * whether the system should be activated.
   * @param enabled value of the enabled flag
   */
  void setEnabled(boolean enabled);

  boolean isEnabled();

  /**
   * Set the name of the system.
   * @param systemName name of the system
   */
  void setName(String systemName);

  /**
   * Return all <code>MessageExchangeDAO</code>.
   *
   * @return all <code>MessageExchangeDAO</code>
   */
  Iterator<MessageExchangeDAO> getAllMessageExchanges();

  /**
   * Set the system archive (SAR) file.
   *
   * @param sarstream
   */
  void setSystemArchive(InputStream sarstream);

  /**
   * Get the saves system archive stream.
   *
   * @return the system archive stream
   */
  InputStream getSystemArchive();

  /**
   * Locate a <code>MessageExchangeDAO</code> by unique id
   *
   * @param instanceId the <code>MessageExchangeDAO</code> id
   *
   * @return the <code>MessageExchangeDAO</code> 
   */
  MessageExchangeDAO getMessageExchange(String instanceId);

  /**
   * Set the system descriptor.
   *
   * @param system the system descriptor
   */
  void setSystemDescriptor(SystemDescriptor system);

  /**
   * Get the system descriptor.
   *
   * @return the system descriptor
   */
  SystemDescriptor getSystemDescriptor();

  /**
   * Get the name of the system
   *
   * @return the not necessarily unique name of the system
   */
  String getSystemName();

  /**
   * Get the system id (primary key)
   *
   * @return the unique identifier for the system
   */
  String getSystemUUID();

  /**
   * Creates a new message exchange.
   *
   * @param instanceId
   * @param operationName
   * @param portType
   * @param channelName
   *
   * @return new message exchange DAO
   */
  MessageExchangeDAO newMessageExchange(String instanceId, Node sourceEpr, Node destEpr,
                                        String operationName, QName portType, String channelName);

  /**
   * Deletes a message exchange based on its instance id.
   *
   * @param instanceId the instance id of the message exchange to be removed
   */
  void removeMessageExchange(String instanceId);

  /** Delete this object from the state store. */
  void delete();

}
