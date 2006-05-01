/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
