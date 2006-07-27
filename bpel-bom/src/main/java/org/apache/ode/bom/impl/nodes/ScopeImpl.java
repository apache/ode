/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.*;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import java.util.*;


/**
 * Base-class for BPEL scope-like constructs (including activities, handlers, etc...).
 */
public abstract class ScopeImpl extends ActivityImpl implements Scope {

  private static final long serialVersionUID = -1L;

  /**
   * Should access to variables be serialized?
   */
  private boolean _serializeVariableAccess;

  /**
   * Variables declared within the scope.
   */
  private final HashSet<Variable> _variables = new HashSet<Variable>();

  /**
   * OCorrelation sets declared within this scope
   */
  private final HashSet<CorrelationSet> _correlationSets = new HashSet<CorrelationSet>();

  private final HashSet<PartnerLink> _partnerLinks = new HashSet<PartnerLink>();

  /**
   * OnEvent event handlers declared within this scope.
   */
  private final ArrayList<OnEvent> _onEventHandlers = new ArrayList<OnEvent>();
  
  /**
   * OnAlarm event handlers declared within this scope
   */
  private final ArrayList<OnAlarm> _alarms = new ArrayList<OnAlarm>();

  /**
   * Compensation handler for the scope
   */
  private CompensationHandlerImpl _compensationHandler;

  /**
   * Fault handler for the scope
   */
  private FaultHandlerImpl _faultHandler;
  
  private TerminationHandlerImpl _terminationHandler;

	
  /**
   * Constructor.
   */
  public ScopeImpl(NSContext nsContext) {
    super(nsContext);
  }

  public ScopeImpl() {
    super();
  }
  
 
  public void setTerminationHandler(TerminationHandler terminationHandler) {
    _terminationHandler = (TerminationHandlerImpl)terminationHandler;
    if(_terminationHandler != null)
      _terminationHandler.setDeclaredIn(this);
  }
  
  public TerminationHandler getTerminationHandler() {
    return _terminationHandler;
  }
  
  public void setCompensationHandler(CompensationHandler compensationHandler) {
    _compensationHandler = (CompensationHandlerImpl) compensationHandler;
    if (_compensationHandler != null)
      _compensationHandler.setDeclaredIn(this);
  }

  public CompensationHandler getCompensationHandler() {
    return _compensationHandler;
  }


  public void setFaultHandler(FaultHandler faultHandler) {
    _faultHandler = (FaultHandlerImpl) faultHandler;
    if (_faultHandler != null)
      _faultHandler.setDeclaredIn(this);
  }

  public FaultHandler getFaultHandler() {
    return _faultHandler;
  }


  public Set<CorrelationSet> getCorrelationSetDecls() {
    return Collections.unmodifiableSet(_correlationSets);
  }

  public void addPartnerLink(PartnerLink partnerLink) {
    ((PartnerLinkImpl) partnerLink).setDeclaredIn(this);
    _partnerLinks.add(partnerLink);
  }

  public PartnerLink getPartnerLink(final String partnerLinkName) {
    return CollectionsX.find_if(_partnerLinks, new MemberOfFunction<PartnerLink>() {
      public boolean isMember(PartnerLink plink) {
        return plink.getName() != null && plink.getName().equals(partnerLinkName);
      }
    });
  }

  public Set<PartnerLink> getPartnerLinks() {
    return Collections.unmodifiableSet(_partnerLinks);
  }

  public Variable getVariableDecl(final String varName) {
    return CollectionsX.find_if(_variables, new MemberOfFunction<Variable>() {
      public boolean isMember(Variable var) {
        
        return var.getName() != null && var.getName().equals(varName);
      }
    });
  }

  public Set<Variable> getVariables() {
    return Collections.unmodifiableSet(_variables);
  }


  public void setVariableAccessSerialized(boolean serialized) {
    _serializeVariableAccess = serialized;
  }

  public boolean isVariableAccessSerialized() {
    return _serializeVariableAccess;
  }

  public boolean isVariableDeclaredInScope(String varName) {
    return getVariableDecl(varName) != null;
  }

  public void addCorrelationSet(CorrelationSet cset) {
    ((CorrelationSetImpl) cset).setDeclaredIn(this);
    _correlationSets.add(cset);
  }


  public void addVariable(Variable var) {
    ((VariableImpl) var).setDeclaredIn(this);
    _variables.add(var);
  }

  public CorrelationSet getCorrelationSetDecl(final String name) {
    return CollectionsX.find_if(_correlationSets, new MemberOfFunction<CorrelationSet>() {
      public boolean isMember(CorrelationSet cset) {
        return cset.getName() != null && cset.getName().equals(name);
      }
    });
  }

  public boolean isCorrelationDeclaredInScope(final String name) {
    return getCorrelationSetDecl(name) != null;
  }


  public List<OnAlarm> getAlarms() {
    return _alarms;
  }

  public List<OnEvent> getEvents() {
    return _onEventHandlers;
  }

  public OnEvent[] getOnEventEventHandlers() {
    return _onEventHandlers.toArray(new OnEvent[]{});
  }
  
  public void addOnAlarmEventHandler(OnAlarm handler) {
    ((OnAlarmImpl) handler).setDeclaredIn(this);
    _alarms.add(handler);
  }

  public void addOnEventHandler(OnEvent handler) {
    ((OnEventImpl) handler).setDeclaredIn(this);
    _onEventHandlers.add(handler);
  }
  
}
