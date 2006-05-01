/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.api;

import javax.xml.namespace.QName;

/**
 * Representation of the BPEL <code>&lt;throw&gt;</code> activity.
 */
public interface ThrowActivity extends Activity {

  /**
   * Set the thrown fault name.
   *
   * @param faultName name of thrown fault
   */
  void setFaultName(QName faultName);

  /**
   * Get the thrown fault name.
   *
   * @return name of thrown fault
   */
  QName getFaultName();

  /**
   * Set the fault variable.
   *
   * @param faultVariable name of the variable containing fault data
   */
  void setFaultVariable(String faultVariable);

  /**
   * Get the fault variable.
   *
   * @return name of variable containing fault data
   */
  String getFaultVariable();
}
