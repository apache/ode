/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.core;

import org.w3c.dom.Node;

/**
 * Interface representing an actual web-service endpoint; this is not the same as
 * a service handle or service, as each service may implement many service
 * endpoints.
 */
public interface ServiceEndpoint {

  /**
   * Checks if the type of the provided node is the right one for this
   * ServiceEndpoint implementation.
   * @param node
   * @return true if the node content matches the service endpoint implementation, false otherwise
   */
  boolean accept(Node node);

  /**
   * URL address of the service endpoint
   * @return url
   */
  String getUrl();

  /**
   * Set service endpoint value from an XML node.
   * @param node
   */
  void set(Node node);

  /**
   * Gets an XML representation of the ServiceEndpoint.
   * @return endpoint node
   */
  Node toXML();

}
