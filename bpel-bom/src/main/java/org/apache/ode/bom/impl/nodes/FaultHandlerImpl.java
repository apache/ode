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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Catch;
import org.apache.ode.bom.api.FaultHandler;
import org.apache.ode.bom.api.Scope;

import java.util.ArrayList;


/**
 * BPEL object model rerpesentation of a fault handler consisting of
 * one or more {@link Catch} objects.
 */
public class FaultHandlerImpl extends BpelObjectImpl implements FaultHandler {

  private static final long serialVersionUID = -1L;

  private ArrayList<Catch> _catches = new ArrayList<Catch>();

  /**
   * For what scope is this a fault handler?
   */
  private Scope _scope;
  

  /**
   * Constructor.
   */
  public FaultHandlerImpl() {
  }

  public Catch[] getCatches() {
    return _catches.toArray(new Catch[_catches.size()]);
  }

  public Scope getScope() {
    return _scope;
  }

  public void addCatch(Catch c) {
    _catches.add(c);
  }

  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _scope = scopeLikeConstruct;
  }
}
