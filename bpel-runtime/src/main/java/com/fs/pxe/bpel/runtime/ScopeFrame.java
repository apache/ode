/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.evt.ScopeEvent;
import com.fs.pxe.bpel.o.OPartnerLink;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.channels.FaultData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;


/**
 * N-tuple representing a scope "frame" (as in stack frame).
 */
class ScopeFrame implements Serializable {
  private static final long serialVersionUID = 1L;

  /** The compiled scope representation. */
  final OScope oscope;

  /** The parent scope frame. */
  final ScopeFrame parent;

  /** Database scope instance identifier. */
  final Long scopeInstanceId;

  Set<CompensationHandler> availableCompensations;

  /** The fault context for this scope. */
  private FaultData _faultData;

  /** Constructor used to create "fault" scopes. */
  ScopeFrame( OScope scopeDef,
              Long scopeInstanceId,
              ScopeFrame parent,
              Set<CompensationHandler> visibleCompensationHandlers,
              FaultData fault) {
    this(scopeDef,scopeInstanceId,parent,visibleCompensationHandlers);
    _faultData = fault;

  }

  public ScopeFrame( OScope scopeDef,
                     Long scopeInstanceId,
                     ScopeFrame parent,
                     Set<CompensationHandler> visibleCompensationHandlers) {
    this.oscope = scopeDef;
    this.scopeInstanceId = scopeInstanceId;
    this.parent = parent;
    this.availableCompensations = visibleCompensationHandlers;
  }


  public ScopeFrame find(OScope scope) {
    if (oscope.name.equals(scope.name)) {
      return this;
    }

    return (parent != null)
           ? parent.find(scope)
           : null;
  }

  public VariableInstance resolve(OScope.Variable variable) {
    return new VariableInstance(find(variable.declaringScope).scopeInstanceId, variable);
  }

  public CorrelationSetInstance resolve(OScope.CorrelationSet cset) {
    return new CorrelationSetInstance(find(cset.declaringScope).scopeInstanceId, cset);
  }

  public PartnerLinkInstance resolve(OPartnerLink partnerLink) {
    return new PartnerLinkInstance(find(partnerLink.declaringScope).scopeInstanceId, partnerLink);
  }

  public String toString() {
    StringBuffer buf= new StringBuffer("{ScopeFrame: o=");
    buf.append(oscope);
    buf.append(", id=");
    buf.append(scopeInstanceId);
    if (availableCompensations != null) {
      buf.append(", avComps=");
      buf.append(availableCompensations);
    }
    if (_faultData != null) {
      buf.append(", fault=");
      buf.append(_faultData);
    }
    buf.append('}');
    return buf.toString();
  }

  public FaultData getFault() {
    if (_faultData != null)
      return _faultData;
    if (parent != null)
      return parent.getFault();
    return null;
  }

  public void fillEventInfo(ScopeEvent event) {
    ScopeFrame currentScope = this;
    ArrayList<String> parentNames = new ArrayList<String>();
    while (currentScope != null) {
      parentNames.add(currentScope.oscope.name);
      currentScope = currentScope.parent;
    }
    event.setParentScopesNames(parentNames);
    if (parent != null)
      event.setParentScopeId(parent.scopeInstanceId);
    event.setScopeId(scopeInstanceId);
    event.setScopeName(oscope.name);
    event.setScopeDeclerationId(oscope.getId());
    if (event.getLineNo() == -1 && oscope.debugInfo !=  null)
      event.setLineNo(oscope.debugInfo.startLine);
  }
}
  