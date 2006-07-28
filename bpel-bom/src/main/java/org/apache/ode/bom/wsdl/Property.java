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
package org.apache.ode.bom.wsdl;

import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

/**
 * BPEL Object Model (BOM) representation of a property declaration.
 */
public interface Property  extends ExtensibilityElement{
  
  /**
   * Get the name of the property.
   *
   * @return property name (namespace qualified)
   */
  QName getName();

  /**
   * Set the name of the property.
   * @param qName property name
   */
  void setName(QName qName);

  /**
   * Get the data type of the property.
   *
   * @return property type (XMLschema type, namespace qualified)
   */
  QName getPropertyType();

  /**
   * Set the data type of the property.
   * @param propertyType property type
   */
  void setPropertyType(QName propertyType);

}
