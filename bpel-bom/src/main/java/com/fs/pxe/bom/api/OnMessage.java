/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.api;


/**
 * <p>
 * Representation of a message-driven event handler.  This is used as part of
 * a <code>pick</code> activity in both the 1.1 and 2.0 dialects, and this is
 * used to represent the <code>onMessage</code> component of an 
 * <code>eventHandlers</code> for a <code>scope</code> or <code>process</code> in
 * 1.1.  In 2.0, the {@link com.fs.pxe.bom.api.OnEvent} is used as
 * part of the <code>eventHandlers</code> construct for a <code>scope</code> or
 * <code>process</code>.
 * </p>
 * <p>
 * Note that the semantics of the variable set with {@link #setVariable(String)} are
 * <em>different</em> depending on whether this is attached to a <code>pick</code>
 * activity or to an <code>eventHandlers</code> for a <code>scope</code> or
 * <code>process</code>.  In the case of the <code>pick</code>, the variable is 
 * interpreted as being declared in the enclosing <code>scope</code>, but for the
 * other construct, the variable is local to the <code>onMessage</code> instance.
 * (Recall that the <code>eventHandlers</code> construct implements replication like
 * the <code>!</code> operator in the pi-calculus, so there may be multiple instances
 * of an <code>onMessage</code> handler around simultaneously.
 * </p>
 * @see com.fs.pxe.bom.api.PickActivity
 * @see com.fs.pxe.bom.api.OnEvent
 */
public interface OnMessage extends Communication, BpelObject {

   
  /**
   * Set the optional message exchange identifier.
   * @param messageExchange
   */
  void setMessageExchangeId(String messageExchange);
  
  /**
   * Get the optional message exchange identifier.
   * @return
   */
  String getMessageExchangeId();
  /**
   * Get the activity associated with the event (i.e. the activity that is activated).
   *
   * @return activity activated when message event occurs
   */
  Activity getActivity();

  /**
   * Set the activity associated with the event (i.e. the activity that is activated).
   *
   * @param activity activated when message event occurs
   */
  void setActivity(Activity activity);

  /**
   * Get the input message variable for the event.
   *
   * @return input message variable
   */
  String getVariable();

  /**
   * Set the input message variable for the event.
   *
   * @param variableName input message variable name
   */
  void setVariable(String variableName);

}
