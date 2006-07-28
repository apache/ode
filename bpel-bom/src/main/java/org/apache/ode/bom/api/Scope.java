/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bom.api;

import java.util.List;
import java.util.Set;

/**
 * Base-class for BPEL scope-like constructs. The term "scope-like construct" encompasses
 * those structures that have the potential to declare either variables, event handlers,
 * fault handlers, or compensation handlers. These scope like constructs include the
 * scope activity, event handlers, the invoke activity, etc...
 */
public interface Scope extends BpelObject {

  /**
   * Set the compensation handler for this scope.
   *
   * @param compensationHandler the compensation handler
   */
  void setCompensationHandler(CompensationHandler compensationHandler);

  /**
   * Get the compensation handler for this scope.
   *
   * @return the compensation handler
   */
  CompensationHandler getCompensationHandler();
  
  /**
   * Sets the termination handler for this scope.
   * @param terminationHandler
   */
  void setTerminationHandler(TerminationHandler terminationHandler);
  
  /**
   * Gets the termination handler for this scope.
   * @return terminationHandler
   */
  TerminationHandler getTerminationHandler();

  /**
   * Determine whether a named correlation is declared in this scope.
   *
   * @param name name of the correlation key.
   * @return <code>true</code> if the correlation is declared in this scope
   */
  boolean isCorrelationDeclaredInScope(String name);

  /**
   * Set the fault handler for this scope.
   *
   * @param faultHandler the fault handler
   */
  void setFaultHandler(FaultHandler faultHandler);

  /**
   * Get the fault handler for this scope.
   *
   * @return the fault handler
   */
  FaultHandler getFaultHandler();

  /**
   * Get correlation sets for this scope.
   *
   * @return correlation sets for this scope.
   */
  Set<CorrelationSet> getCorrelationSetDecls();

  /**
   * Add a correlation set to this scope.
   *
   * @param cset correlation set
   */
  void addCorrelationSet(CorrelationSet cset);

  /**
   * Get a correlation set decleration.
   *
   * @param setName name of correlation set
   * @return {@link CorrelationSet} of the fiven name.
   */
  CorrelationSet getCorrelationSetDecl(String setName);

  /**
   * DOCUMENTME
   *
   * @param varName DOCUMENTME
   * @return DOCUMENTME
   */
  Variable getVariableDecl(String varName);

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  Set <Variable> getVariables();

  /**
   * Get an array of <code>OnAlarmEventHandler</code>s for this scope.
   *
   * @return the <code>OnAlarmEventHandler</code>s
   */
  List<OnAlarm> getAlarms();

  /**
   */
  List<OnEvent> getEvents();

  /**
   */
  void setVariableAccessSerialized(boolean serialized);

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  boolean isVariableAccessSerialized();

  /**
   * Determine whether a variable is declared in this scope.
   *
   * @param varName the variable name
   * @return <code>true</code> if the variable is declared in this scope
   */
  boolean isVariableDeclaredInScope(String varName);


  /**
   * Add an <code>OnAlarmEventHandler</code> to this scope.
   *
   * @param handler the <code>OnAlarmEventHandler</code>
   */
  void addOnAlarmEventHandler(OnAlarm handler);


  /**
   * <p>
   * Add an <code>onEvent</code> to this scope.
   * for the 1.1 version.
   * </p> 
   * @param handler the <code>OnEventEventHandler</code> to add.
   */
  void addOnEventHandler(OnEvent handler);
  
  /**
   * Add a variable in this scope.
   *
   * @throws IllegalArgumentException thrown if a variable has already been defined
   *                                  with the same name as the new variable
   */
  void addVariable(Variable var);

  /**
   * Add a partnerLink link to the list of partnerLink links.
   *
   * @param partnerLink the new {@link PartnerLink}.
   * @throws IllegalStateException if a partnerLink with the same name has already been added to the process
   */
  void addPartnerLink(PartnerLink partnerLink);

  /**
   * Get a partnerLink declared in this scope.
   *
   * @param partnerLinkName name of partner link
   */
  PartnerLink getPartnerLink(String partnerLinkName);

  /**
   * Get all partnerLinks delcared in this scope.
   *
   * @return set of declared {@link PartnerLink}s.
   */
  Set<PartnerLink> getPartnerLinks();

}
