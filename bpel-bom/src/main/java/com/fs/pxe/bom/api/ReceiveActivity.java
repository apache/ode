/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;

/**
 * Representation of a BPEL <code>&lt;receive&gt;</code> activity.
 */
public interface ReceiveActivity extends CreateInstanceActivity, Communication {

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
   * Set the name of the variable that will hold the input message.
   *
   * @param variable name of input message variable
   */
  void setVariable(String variable);

  /**
   * Get the name of the variable that will hold the input message.
   *
   * @return name of input message variable
   */
  String getVariable();
}
