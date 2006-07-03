/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

import javax.xml.namespace.QName;

/**
 * Event indicating the creation of a new process instance as part of an
 * uncorrelated inbound message to create instance operation.
 */
public class NewProcessInstanceEvent extends ProcessMessageExchangeEvent {
  private static final long serialVersionUID = 1L;

  private Long _rootScopeId;
  private int _scopeDeclarationId;

  public NewProcessInstanceEvent() {
    super();
  }
  
  public NewProcessInstanceEvent(
    QName processName, QName processId, Long processInstanceId) {
    super(PROCESS_INPUT,processName,processId, processInstanceId);
  }

  public Long getRootScopeId() {
    return _rootScopeId;
  }

  public void setRootScopeId(Long rootScopeId) {
    _rootScopeId = rootScopeId;
  }

  public int getScopeDeclarationId() {
    return _scopeDeclarationId;
  }

  public void setScopeDeclarationId(int scopeDeclarationId) {
    _scopeDeclarationId = scopeDeclarationId;
  }

}
