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
package org.apache.ode.jbi;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class TestOdeJbiComponentLifeCycle extends TestCase {

    JBIContainer container;
    File rootDir, installDir, odeDir;
    TransactionManager txm;
    private OdeComponent component;

    protected void setUp() throws Exception {
        rootDir = File.createTempFile("smixInstallDir","");
        installDir = new File(rootDir,"install");
        installDir.mkdir();
        txm = new EmbeddedGeronimoFactory().getTransactionManager();
        odeDir = new File(installDir,"ODE");
        odeDir.mkdir();

        container = new JBIContainer();
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.setEmbedded(true);


        container.setTransactionManager(txm);
        container.init();
        container.start();

        component = new OdeComponent();

    }

    protected void tearDown() throws Exception {
        // Make sure to shutdown ODE, otherwise DB will lock up
        try {
            container.deactivateComponent("ODE");
        } catch (Exception ex) {
            ;//ok ignore
        }

        if (container != null) {
            container.shutDown();
            container = null;
        }

        component = null;


    }

    public void testComponentLifeCycle() throws Exception {
        container.activateComponent(component,"ODE");
        container.deactivateComponent("ODE");
    }

    public void testProcessLifeCycle() throws Exception {
        container.activateComponent(component, "ODE");
        container.start();

        // For lack of a better way of doing this:
        component.getServiceUnitManager().deploy("HelloWorld", "../distro-jbi/src/examples/HelloWorld2/HelloWorld2-process");
        component.getServiceUnitManager().init("HelloWorld", "../distro-jbi/src/examples/HelloWorld2/HelloWorld2-process");
        component.getServiceUnitManager().start("HelloWorld");
        component.getServiceUnitManager().stop("HelloWorld");
        component.getServiceUnitManager().undeploy("HelloWorld", "../distro-jbi/src/examples/HelloWorld2/HelloWorld2-process");

        container.deactivateComponent("ODE");

        container.stop();

    }


    public void testHelloWorld() throws Exception {
        OdeComponent component = new OdeComponent();
        container.activateComponent(component, "ODE");

        // For lack of a better way of doing this:
        component.getServiceUnitManager().deploy("HelloWorld", "../jbi-examples/src/examples/HelloWorld2/HelloWorld2-process");
        component.getServiceUnitManager().init("HelloWorld", "../jbi-examples/src/examples/HelloWorld2/HelloWorld2-process");
        component.getServiceUnitManager().start("HelloWorld");
        DefaultServiceMixClient client = new DefaultServiceMixClient(container);
        InOut io = client.createInOutExchange();
        io.setService(new QName("urn:/HelloWorld2.wsdl", "HelloService"));
        io.setOperation(new QName("urn:/HelloWorld2.wsdl", "Hello"));
        io.getInMessage().setContent(new StreamSource(getClass().getResourceAsStream("/HelloWorldRequest.xml")));
        client.sendSync(io,20000);
        assertEquals(ExchangeStatus.ACTIVE,io.getStatus());
        assertNotNull(io.getOutMessage());
        assertNotNull(io.getOutMessage().getContent());

    }


}
