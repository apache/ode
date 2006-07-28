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
package org.apache.ode.bpel.evt;

import java.util.List;

/**
 * Event related to a process instance scope.
 */
public abstract class ScopeEvent extends ProcessInstanceEvent {
  private Long _scopeId;
  private Long _parentScopeId;
  private String _scopeName;
  private int _scopeDeclarationId;
  private List<String> parentScopesNames;

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

  public List<String> getParentScopesNames() {
    return parentScopesNames;
  }

  public void setParentScopesNames(List<String> parentScopesNames) {
    this.parentScopesNames = parentScopesNames;
  }

  public TYPE getType() {
    return TYPE.scopeHandling;
  }
}
