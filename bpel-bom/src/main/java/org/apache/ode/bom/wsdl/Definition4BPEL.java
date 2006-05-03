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
package org.apache.ode.bom.wsdl;

import java.util.List;

import javax.wsdl.Definition;
import javax.xml.namespace.QName;

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
   * Get a list of the defined {@link org.apache.ode.bom.wsdl.Property}s.
   *
   * @return {@link List} of {@link org.apache.ode.bom.wsdl.Property} objects
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
