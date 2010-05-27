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
package org.apache.ode.bpel.compiler;

import java.io.File;
import java.net.URL;

import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactoryBPEL11;
import org.apache.ode.utils.xsd.SchemaModel;

public class WSDLRegistryTest extends TestCase {

  private WSDLRegistry _registry;

  public WSDLRegistryTest() {
    super();
  }

  protected void setUp() throws Exception
  {
    super.setUp();
    _registry = new WSDLRegistry(null);
  }

  protected void tearDown() throws Exception
  {
    _registry = null;
    super.tearDown();
  }

  public void testInitialModel() throws Exception
  {
    // access initial dummy model
    SchemaModel m = _registry.getSchemaModel();
    assertNotNull(m);

    // verify dummy model
    QName type = new QName("http://fivesight.com/bogus/namespace", "__bogusType__");
    assertTrue(m.knowsSchemaType(type));
  }

  public void testAddWSDL() throws Exception {
    URL wsd = getClass().getResource("/1.1/good/test.wsdl");

    // load & register wsdl
    WSDLFactory4BPEL factory = (WSDLFactory4BPEL)WSDLFactoryBPEL11.newInstance();
    WSDLReader reader = factory.newWSDLReader();

    //ResourceFinder finder = new DefaultResourceFinder(new File(wsd.getPath()).getParentFile());
    File parent = new File(wsd.toURI().getPath()).getParentFile();
    ResourceFinder finder = new DefaultResourceFinder(parent, parent);
    WSDLLocatorImpl loc = new WSDLLocatorImpl(finder,wsd.toURI());
    Definition4BPEL wsdl = (Definition4BPEL) reader.readWSDL(loc);
    _registry.addDefinition(wsdl, finder, wsd.toURI());

    // access model
    SchemaModel m = _registry.getSchemaModel();
    assertNotNull(m);

    assertTrue("WSDL-Define type not visible.",m.knowsSchemaType(new QName("uri:testing", "TComplex1")));
    assertTrue("Type from import not visible.",m.knowsSchemaType(new QName("uri:test1", "TComplex2")));


  }

}
