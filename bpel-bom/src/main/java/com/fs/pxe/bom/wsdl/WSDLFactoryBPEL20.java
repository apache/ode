/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bom.wsdl;

import com.fs.pxe.bom.api.Constants;

import javax.wsdl.Definition;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.namespace.QName;

/**
 * Factory for {@link WSDLFactory} objects that are pre-configured to handle
 * BPEL 2.0 extension elements.
 */
public class WSDLFactoryBPEL20 extends WSDLFactoryImpl implements WSDLFactory4BPEL {

  public WSDLFactoryBPEL20() {
    super(Constants.NS_WSBPEL_2004_03, Constants.NS_WSBPEL_PARTNERLINK_2004_03);
  }

  public static WSDLFactory newInstance() {
    return new WSDLFactoryBPEL20();
  }

  public ExtensionRegistry newPopulatedExtensionRegistry() {
    ExtensionRegistry extRegistry;
    extRegistry = super.newPopulatedExtensionRegistry();
    extRegistry.registerDeserializer(Definition.class, new QName(_bpwsNS, "property"),
                             new PropertySerializer());
    extRegistry.registerDeserializer(Definition.class, new QName(_bpwsNS, "propertyAlias"),
                             new PropertyAliasSerializer_20());
    extRegistry.registerDeserializer(Types.class, XMLSchemaType.QNAME,
                             new XMLSchemaTypeSerializer());
    extRegistry.registerDeserializer(Definition.class, new QName(Constants.NS_WSBPEL_PARTNERLINK_2004_03, "partnerLinkType"),
        new PartnerLinkTypeSerializer_2_0(Constants.NS_WSBPEL_PARTNERLINK_2004_03));
    return extRegistry;

  }

}
