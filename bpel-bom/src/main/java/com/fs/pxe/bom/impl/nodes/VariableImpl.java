/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.impl.nodes;

import com.fs.pxe.bom.api.Scope;
import com.fs.pxe.bom.api.Variable;

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
