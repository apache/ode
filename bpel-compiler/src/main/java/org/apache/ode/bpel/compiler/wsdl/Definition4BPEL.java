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
package org.apache.ode.bpel.compiler.wsdl;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.compiler.bom.PartnerLinkType;
import org.apache.ode.bpel.compiler.bom.Property;
import org.apache.ode.bpel.compiler.bom.PropertyAlias;

import java.util.List;

/**
 * Extension of the WSDL {@link Definition} interface that includes
 * manipulators for BPEL-specific extensions.
 */
public interface Definition4BPEL extends Definition {

  /**
   * Get a list of the defined {@link PartnerLinkType}s.
   *
   * @return {@link List} of {@link PartnerLinkType} objects
   */
  List<PartnerLinkType> getPartnerLinkTypes();

  /**
   * Get a list of the defined {@link org.apache.ode.bpel.compiler.bom.Property}s.
   *
   * @return {@link List} of {@link org.apache.ode.bpel.compiler.bom.Property} objects
   */
  List<Property> getProperties();

  /**
   * Get a declared BPEL property, by name.
   * @param name property name
   * @return the {@link Property} or <code>null</code> if not defined
   */
  Property getProperty(QName name);

  /**
   * Get a list of the schema types defined in-line.
   *
   * @return {@link List} of {@link org.apache.ode.utils.wsdl.XMLSchemaType} objects
   */
  List <XMLSchemaType> getSchemas();

  /**
   * Get a list of the defined {@link PropertyAlias}es.
   *
   * @return {@link List} of {@link PropertyAlias} objects
   */
  List<PropertyAlias> getPropertyAliases();


  /**
   * Get the property alias for the given property name and message type.
   * @param propertyName property name
   * @param messageType message type
   * @return matching {@link PropertyAlias} or <code>null</code> if not found
   */
  PropertyAlias getPropertyAlias(QName propertyName, QName messageType);

  PartnerLinkType getPartnerLinkType(QName partnerLinkType);

  Definition getDefinition();
}
