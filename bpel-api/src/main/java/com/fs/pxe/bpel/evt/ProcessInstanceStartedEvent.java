/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

/**
 * Event indicating the start (post creation) of a new process instance.
 */
public class ProcessInstanceStartedEvent extends ProcessInstanceEvent {

  private static final long serialVersionUID = -4245567193743622300L;
  private Long _rootScopeId;
  private int _scopeDeclarationId;

  public ProcessInstanceStartedEvent() {
    super();
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
