/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl;

import com.fs.utils.uuid.UUID;

import java.io.Serializable;
import java.util.Date;

abstract class DomainTask implements Serializable {
  private String _domainId;
  private final String _taskId = new UUID().toString();
  private boolean _persistent = true;
  private Date _deliveryDate;

  public DomainTask(String domainId){
    _domainId = domainId;
  }

  public String getId() {
    return _taskId;
  }
  
  public Date getDeliveryDate() {
    return _deliveryDate;
  }

  public void setDeliveryDate(Date deliveryDate) {
    _deliveryDate = deliveryDate;
  }

  /**
   * Set the unique domainId ID for this message.
   *
   * @param domainId domainId identifier
   */
  public void setDomainId(String domainId){
  	_domainId = domainId;
  }

  /**
   * Get the unique domain ID for this message.
   *
   * @return the domain ID
   */
  public String getDomainId(){
  	return _domainId;
  }

  public boolean isPersistent() {
    return _persistent;
  }

  public void setPersistent(boolean persistent) {
    _persistent = persistent;
  }
}
