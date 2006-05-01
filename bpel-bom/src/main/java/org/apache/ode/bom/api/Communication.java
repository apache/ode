/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

import java.util.List;

import javax.xml.namespace.QName;

/**
 * Base interface for BPEL constructs representing a web-service communication.
 */
public interface Communication {

  /**
   * Set the operation for the communication.
   *
   * @param operation name of operation
   */
  void setOperation(String operation);

  /**
   * Get the operation for the communication.
   *
   * @return name of operation
   */
  String getOperation();

  /**
   * Set the partner link on which to communicate.
   *
   * @param partnerLink name of the partner link
   */
  void setPartnerLink(String partnerLink);

  /**
   * Get the partnerLink link on which to communicate.
   *
   * @return name of the partner link
   */
  String getPartnerLink();

  /**
   * Set the port type for the communication. This is optional as the partner link type
   * implies a port type.
   *
   * @param portType
   */
  void setPortType(QName portType);

  /**
   * Get the port type for the communication. This property is optional as the partner link type
   * implies a port type.
   *
   * @return name of portType for the communication (or <code>null</code>)
   */
  QName getPortType();

  /**
   * Add a correlation for this communication.
   *
   * @param correlation correlation to apply on the communication
   */
  void addCorrelation(Correlation correlation);


  /**
   * Get the correlations applicable to this communication.
   *
   * @param patternMask bit-mask of the application pattern
   * @return correlations applicable to the communication
   */
  List<Correlation> getCorrelations(short patternMask);

}
