/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib.bpel.hobj;

import org.apache.ode.daohib.hobj.HObject;

/**
 * Efficient storage of properties (bpel properties).
 * Useful for identification of process instances based
 * on indexed lookup of property values.
 * 
 * @hibernate.class
 *  table="VAR_PROPERTY"
 */
public class HVariableProperty extends HObject{
 
  private String _propertyValue;
  private String _propertyName;
  private HXmlData _variable;
  
  /**
   * 
   */
  public HVariableProperty() {
    super();
  }
  
  public HVariableProperty(HXmlData var, String name, String value){
  	_variable = var;
    _propertyName = name;
    _propertyValue = value;
  }
  /**
   * @hibernate.many-to-one
   *  column="XML_DATA_ID"
   */
  public HXmlData getXmlData(){
  	return _variable;
  }
  
  public void setXmlData(HXmlData xmldata){
  	_variable = xmldata;
  }
  
  /**
   * @hibernate.property
   *  column="PROP_VALUE"
   *  index="PROP_VALUE_IDX"
   */
  public String getValue() {
    return _propertyValue;
  }
  public void setValue(String value) {
    _propertyValue = value;
  }
  /**
   * @hibernate.property
   *  column="PROP_NAME"
   *  type="string"
   *  length="255"
   *  not-null="true"
   *  index="PROP_NAME_IDX"
   */
  public String getName() {
    return _propertyName;
  }
  public void setName(String name) {
    _propertyName = name;
  }
  
}
