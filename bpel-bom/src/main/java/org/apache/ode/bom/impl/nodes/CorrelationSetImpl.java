/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.CorrelationSet;
import org.apache.ode.bom.api.Scope;

import javax.xml.namespace.QName;


/**
 * BPEL Object Model representation of a correlation set.
 */
public class CorrelationSetImpl extends BpelObjectImpl implements CorrelationSet {

  private static final long serialVersionUID = -1L;

  private String _name;
  private QName[] _properties;
  private ScopeImpl _declaredIn;
 
  public CorrelationSetImpl() {
  }

  public Scope getDeclaringScope() {
    return _declaredIn;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public QName[] getProperties() {
    return _properties;
  }

  public void setProperties(QName[] properties) {
    _properties = properties;
  }

  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _declaredIn = scopeLikeConstruct;
  }

}
