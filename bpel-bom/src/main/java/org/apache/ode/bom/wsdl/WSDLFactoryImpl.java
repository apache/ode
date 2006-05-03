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

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;

/**
 * Implementation of the BPEL-aware {@link javax.wsdl.factory.WSDLFactory}.
 */
abstract class WSDLFactoryImpl extends WSDLFactory implements WSDLFactory4BPEL {
  private WSDLFactory _wsdlFactory;

  /** BPEL Namespace (<code>bpws</code>). */
  protected String _bpwsNS;

  /** Partner Link Namespace (<code>plnk</code>). */
  private String _plnkNS;

  WSDLFactoryImpl(String bpwsNS, String plnkNS)  {
    try {
      _wsdlFactory  = WSDLFactory.newInstance();
    } catch (WSDLException e) {
      throw new AssertionError("Unable to load WSDL4J!");
    }
    
    _bpwsNS = bpwsNS;
    _plnkNS = plnkNS;

  }

  /**
   * Create a {@link Definition4BPEL} object out of a standard
   * WSDL {@link Definition}.
   * @param wsdlDef WSDL {@link Definition}
   * @return new {@link Definition4BPEL} object
   */
  public Definition4BPEL narrow(Definition wsdlDef) {
    if (wsdlDef instanceof Definition4BPEL)
      return (Definition4BPEL) wsdlDef;
    return new Definition4BPELImpl(wsdlDef, _bpwsNS, _plnkNS);
  }

  public Definition newDefinition() {
    Definition def = _wsdlFactory.newDefinition();
    def.setExtensionRegistry(newPopulatedExtensionRegistry());
    return new Definition4BPELImpl(def, _bpwsNS, _plnkNS);
  }


  public WSDLReader newWSDLReader() {
    WSDLReader reader = _wsdlFactory.newWSDLReader();
    reader.setFactoryImplName(getClass().getName());
    reader.setFeature("javax.wsdl.verbose", false);
    reader.setExtensionRegistry(newPopulatedExtensionRegistry());
    return reader;
  }

  public WSDLWriter newWSDLWriter() {
    WSDLWriter writer = _wsdlFactory.newWSDLWriter();
    writer.setFeature("javax.wsdl.verbose", false);
    return writer;
  }

  public ExtensionRegistry newPopulatedExtensionRegistry() {
    return _wsdlFactory.newPopulatedExtensionRegistry();
  }
}
