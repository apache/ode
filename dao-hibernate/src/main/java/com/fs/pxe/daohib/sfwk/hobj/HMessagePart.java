/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk.hobj;

import com.fs.pxe.daohib.hobj.HObject;
import com.fs.pxe.daohib.hobj.HLargeData;

/**
 * Hibernate-managed table for keeping track of message parts.
 *
 * @see HSfwkMessage
 *
 * @hibernate.class
 *    table="PXE_MESSAGE_PART"
 *    dynamic-update="true"
 *    
 */
public class HMessagePart extends HObject {
  private HSfwkMessage _message;
  private String _part;
  private HLargeData _data;

  public HMessagePart() {}

  public HMessagePart(HSfwkMessage message, String partName) {
    _message = message;
    _part = partName;
  }

  /**
   * The XML/Character data of this part.
   * @hibernate.many-to-one column="LDATA_ID" cascade="delete"
   */
  public HLargeData getData() {
    return _data;
  }

  public void setData(HLargeData data) {
    _data = data;
  }

  /**
   * The {@link HSfwkMessage} to which this part belongs.
   * @hibernate.many-to-one 
   * @hibernate.column name="MESSAGE_ID" index="IDX_MESSAGE_PART_MESSAGE"
   */
  public HSfwkMessage getMessage() {
    return _message;
  }

  public void setMessage(HSfwkMessage message) {
    _message = message;
  }

  /**
   * The name of the part.
   * @hibernate.property column="PARTNAME"
   */
  public String getPart() {
    return _part;
  }

  public void setPart(String part) {
    _part = part;
  }
}
