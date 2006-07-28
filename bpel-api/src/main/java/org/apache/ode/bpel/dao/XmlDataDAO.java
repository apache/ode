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
package org.apache.ode.bpel.dao;

import org.w3c.dom.Node;


/**
 * Data access object representing a piece of XML data. This is object is used
 * to model BPEL variables.
 */
public interface XmlDataDAO {

  /**
   * Get the name of the variable.
   * @return variable name
   */
  public String getName();


  /**
   * Checks if the dao has been assigned any data.
   *
   * @return <code>true</code> is assignment has NOT occured.
   */
  public boolean isNull();

  
  /**
   * Retreive the variable data.
   *
   * @return the variable data
   */
  public Node get();

  /**
   * Remove the object from the data store.
   */
  public void remove();

  /**
   * Set the data value of a variable.
   *
   * @param val value
   */
  public void set(Node val);

  /**
   * Return the value of a property.  Properties are useful
   * for extracting simple type data which can be used for querying
   * and identifying process instances. 
   * @param propertyName
   * @return value of property or <b>null</b> if not set.
   */
  public String getProperty(String propertyName);
    
  /**
   * Sets the value of a property
   * @param pname
   * @param pvalue
   */
  public void setProperty(String pname, String pvalue);
  
  /**
   * Gets the scope associated with this xml data.
   * @return scope
   */
  public ScopeDAO getScopeDAO();

}
