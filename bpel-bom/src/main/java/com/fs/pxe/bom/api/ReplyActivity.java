/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

import javax.xml.namespace.QName;

/**
 * Representation of the BPEL <code>&lt;reply&gt;</code> activity.
 */
public interface ReplyActivity extends Activity, Communication {

  /**
   * Set the optional message exchange identifier
   * @param messageExchange
   */
  void setMessageExchangeId(String messageExchange);
  
  /**
   * Get the optional message exchange identifier.
   * @return
   */
  String getMessageExchangeId();
  
  /**
   * Set the fault name with which to reply.
   *
   * @param name the fault name or <code>null</code> to disable fault reply.
   */
  void setFaultName(QName name);

  /**
   * Get the fault name with which to reply.
   *
   * @return the fault name
   */
  QName getFaultName();


  /**
   * Set the variable containing the reply message.
   *
   * @param variable name of variable containing the reply message
   */
  void setVariable(String variable);

  /**
   * Get the variable containing the reply message.
   *
   * @return name of variable containing the reply message
   */
  String getVariable();


}
