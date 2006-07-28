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

import org.apache.ode.bom.api.Scope;
import org.apache.ode.bom.api.Variable;

import java.io.Serializable;

import javax.xml.namespace.QName;


/**
 * BPEL VariableImpl Definition.
 */
public class VariableImpl extends BpelObjectImpl implements Serializable, Variable {

  private static final long serialVersionUID = -1L;

  /**
   * Name of the variable.
   */
  private String _name;

  /**
   * Type of the variable (element)
   */
  private QName _type;

  private short _declType;

  /**
   * The scope to which this variable belongs
   */
  private Scope _scope;

  /**
   * If a schemaType, is type simple
   */
  private boolean _isSimpleType;
  
  public VariableImpl() {
  }

  public VariableImpl(String name) {
    _name = name;
  }

  public VariableImpl(String name, QName schemaType, boolean isSimpleType) {
    _name = name;
    _type = schemaType;
    _isSimpleType = isSimpleType;
  }


  public Scope getDeclaringScope() {
    return _scope;
  }


  public void setMessageType(QName messageType) {
    _type = messageType;
    _declType = Variable.TYPE_MESSAGE;
  }

  public void setSchemaType(QName schemaType) {
    _type = schemaType;
    _declType = Variable.TYPE_SCHEMA;
  }

  public void setElementType(QName elementType) {
    _type = elementType;
    _declType = Variable.TYPE_ELEMENT;
  }

  public String getName() {
    return _name;
  }

  public void setName(String varName) {
    _name = varName;
  }

  public QName getTypeName() {
    return _type;
  }

  public short getDeclerationType() {
    return _declType;
  }

  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _scope = scopeLikeConstruct;
  }

}
