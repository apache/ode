/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.dao;

import com.fs.pxe.bpel.common.BpelEventFilter;
import com.fs.pxe.bpel.evt.BpelEvent;

import java.util.Collection;
import java.util.List;


/**
 * Data access objec representing a BPEL scope instance.
 * Objects of this class manage a collection of correlation
 * sets and XML data variables.
 */
public interface ScopeDAO  {

  /**
   * Get the unique identifier for this scope instance.
   * @return scope instance id
   */
  Long getScopeInstanceId();

  /**
   * Get the scope model id from the object
   * @return scope model id
   */
  int getModelId();


  /**
   * Get scope name (from the definition / or auto-generated).
   * NOTE: the scope names are not necessarily unique. 
   * @return scope name
   */
  String getName();

  /**
   * Get a correlation set by name.
   * @param corrSetName correlation set name
   * @return correlation set instance
   */
  CorrelationSetDAO getCorrelationSet(String corrSetName);

  /**
   * Gets all correlation sets for this scope
   * @return correlation set instances
   */
  Collection<CorrelationSetDAO> getCorrelationSets();

  /**
   * Get the parent scope.
   * @return parent scope
   */
  ScopeDAO getParentScope();

  Collection<ScopeDAO> getChildScopes();

  /**
   * Get the process instance to which this scope belongs.
   * @return owner {@link ProcessInstanceDAO}
   */
  ProcessInstanceDAO getProcessInstance();

  /**
   * Set current state of the scope.
   * @param state new scope state
   */
  void setState(ScopeStateEnum state);

  /**
   * Get current state of the scope.
   * @return current scope state
   */
  ScopeStateEnum getState();

  /**
   * Get a variable by name.
   * @param varName variable name
   * @return {@link XmlDataDAO} object representing the requested variable
   */
  XmlDataDAO getVariable(String varName);

  /**
   * Get a colleciton of all the variables belonging to this scope. 
   * @return collection of variables
   */
  Collection<XmlDataDAO> getVariables();

  /**
   * Get an ordered list of events associated with this scope.
   * @return collection of bpel events.
   */
  List<BpelEvent> listEvents(BpelEventFilter efilter);

  /**
   * Create a storage space for partner link values for the scope. 
   * @param plinkModelId partner link model id
   * @param pLinkName partner link name
   * @return {@link PartnerLinkDAO} object representing the created endpoint reference
   */
  PartnerLinkDAO createPartnerLink(int plinkModelId, String pLinkName, String myRole, String partnerRole);

  /**
   * Get the parnter link storage object associated with this scope instance
   * and the provided partner link model id.
   * @param plinkModelId partner link model id 
   * @return {@link PartnerLinkDAO} object representing the requested endpoint reference
   */
  PartnerLinkDAO getPartnerLink(int plinkModelId);
}
