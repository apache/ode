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
