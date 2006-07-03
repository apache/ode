/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.evt;

/**
 * Event related to a process instance scope.
 */
public abstract class ScopeEvent extends ProcessInstanceEvent {
  private Long _scopeId;
  private Long _parentScopeId;
  private String _scopeName;
  private int _scopeDeclarationId;

  public ScopeEvent() {
    super();
  }

  public Long getScopeId() {
    return _scopeId;
  }

  public void setScopeId(Long scopeId) {
    _scopeId = scopeId;
  }

	/**
	 * @param scopeName The scopeName to set.
	 */
	public void setScopeName(String scopeName) {
		_scopeName = scopeName;
	}

	/**
	 * @return Returns the scopeName.
	 */
	public String getScopeName() {
		return _scopeName;
	}

  public int getScopeDeclarationId() {
   return _scopeDeclarationId; 
  }
	
  /**
	 * @param id
	 */
	public void setScopeDeclerationId(int id) {
    _scopeDeclarationId = id;
	}

  public void setParentScopeId(Long parentScopeId) {
    _parentScopeId = parentScopeId;
  }
  
  public Long getParentScopeId() {
    return _parentScopeId;
  }

}
