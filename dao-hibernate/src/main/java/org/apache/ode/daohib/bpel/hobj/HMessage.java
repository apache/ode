/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib.bpel.hobj;

import org.apache.ode.daohib.hobj.HLargeData;
import org.apache.ode.daohib.hobj.HObject;

/**
 * Hibernate-managed table for keeping track of messages.
 *
 * @hibernate.class
 *  table="BPEL_MESSAGE"
 */
public class HMessage extends HObject {

  private HMessageExchange _mex;
  private String _type;
  private HLargeData _data;
  
  
  public void setMessageExchange(HMessageExchange mex) {
    _mex = mex;
  }
  
  /** @hibernate.many-to-one column="MEX" */
  public HMessageExchange getMessageExchange() {
    return _mex;
  }

  public void setType(String type) {
    _type = type;
  }

  /** @hibernate.property column="TYPE" */
  public String getType() {
    return _type;
  }

  /** @hibernate.many-to-one column="DATA" */
  public HLargeData getMessageData() {
    return _data;
  }

  public void setMessageData(HLargeData data) {
    _data = data;
  }

}
