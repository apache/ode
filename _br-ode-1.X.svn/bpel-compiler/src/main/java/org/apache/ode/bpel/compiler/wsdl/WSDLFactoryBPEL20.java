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

import org.apache.ode.bpel.compiler.bom.Bpel20QNames;
import org.apache.ode.bpel.compiler.bom.BpelObjectFactory;

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

    private BpelObjectFactory _bomf;
    private BpelExtensionSerializer _bs;

    public WSDLFactoryBPEL20() {
        super(Bpel20QNames.NS_WSBPEL2_0_FINAL_EXEC, Bpel20QNames.NS_WSBPEL2_0_FINAL_PLINK
                , Bpel20QNames.NS_WSBPEL2_0_FINAL_VARPROP);
        _bomf = BpelObjectFactory.getInstance();
        _bs = new BpelExtensionSerializer(_bomf);
    }

    public static WSDLFactory newInstance() {
        return new WSDLFactoryBPEL20();
    }

    public ExtensionRegistry newPopulatedExtensionRegistry() {
        ExtensionRegistry extRegistry;
        extRegistry = super.newPopulatedExtensionRegistry();
        extRegistry.registerDeserializer(Definition.class, new QName(_propNS, "property"),_bs);
        extRegistry.registerDeserializer(Definition.class, new QName(_propNS, "propertyAlias"), _bs);
        extRegistry.registerDeserializer(Types.class, XMLSchemaType.QNAME, new XMLSchemaTypeSerializer());
        extRegistry.registerDeserializer(Definition.class, new QName(_plnkNS, "partnerLinkType"),_bs);
        return extRegistry;

    }

}
