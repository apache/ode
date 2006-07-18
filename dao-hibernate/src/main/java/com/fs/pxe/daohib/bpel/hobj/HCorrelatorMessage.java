/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel.hobj;

import com.fs.pxe.daohib.hobj.HObject;
import java.util.HashSet;
import java.util.Set;

/**
 * @hibernate.class table="BPEL_CORRELATOR_MESSAGE"
 */
public class HCorrelatorMessage extends HObject {
  
	private Set<HCorrelatorMessageKey> _correlationKeys = new HashSet<HCorrelatorMessageKey>();
  private HCorrelator _correlator;
  private HMessageExchange _messageExchange;

	public HCorrelatorMessage() {
		super();
	}
  
	/**
   * @hibernate.set
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="CORRELATOR_MESSAGE_ID"
   * @hibernate.collection-one-to-many
   *  class="com.fs.pxe.daohib.bpel.hobj.HCorrelatorMessageKey"
   */
  public Set<HCorrelatorMessageKey> getCorrelationHashKeys() {
    return _correlationKeys;
  }

  public void setCorrelationHashKeys(Set<HCorrelatorMessageKey> correlationHashKeys) {
    _correlationKeys = correlationHashKeys;
  }
  
  /**
   * @hibernate.many-to-one
   * @hibernate.column name="CORRELATOR" index="IDX_CORRELATORMESSAGE_CID"
   */
  public HCorrelator getCorrelator() {
    return _correlator;
  }

  public void setCorrelator(HCorrelator correlator) {
    _correlator = correlator;
  }

  /**
   * @hibernate.many-to-one column="MEX" 
   */
  public HMessageExchange getMessageExchange() {
    return _messageExchange;
  }

  public void setMessageExchange(HMessageExchange data) {
    _messageExchange = data;
  }

}
