/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk.hobj;

import com.fs.pxe.daohib.hobj.HObject;

import java.util.Collection;
import java.util.HashSet;

/**
 * Hibernate-managed table for keeping track of PXE messages.
 *
 * @hibernate.class
 *  table="PXE_MESSAGE"
 */
public class HSfwkMessage extends HObject {

  private Collection<HMessagePart> _parts = new HashSet<HMessagePart>();

  /**
   * The message parts.
   * @hibernate.bag
   *   lazy="true"
   *   inverse="true"
   *   cascade="delete"
   * @hibernate.collection-key
   *  column="MESSAGE_ID"
   * @hibernate.collection-one-to-many
   *   class="com.fs.pxe.daohib.sfwk.hobj.HMessagePart"
   */
  public Collection<HMessagePart> getParts() {
    return _parts;
  }

  public void setParts(Collection<HMessagePart> parts) {
    _parts = parts;
  }

}
