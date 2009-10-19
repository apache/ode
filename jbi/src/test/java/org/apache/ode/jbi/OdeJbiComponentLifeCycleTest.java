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

import junit.framework.TestCase;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.framework.ComponentContextImpl;
import org.apache.servicemix.jbi.framework.ComponentNameSpace;
import org.junit.Ignore;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.File;

@Ignore
public class OdeJbiComponentLifeCycleTest extends TestCase {

    JBIContainer container;
    File rootDir, odeDir;
    TransactionManager txm;
    private OdeComponent component;
    private static String helloWorldDir = "target/test/resources/HelloWorldJbiTest";

    protected void setUp() throws Exception {
        rootDir = new File("target/test/smx");
        odeDir = new File(rootDir, "ode");
        txm = new EmbeddedGeronimoFactory().getTransactionManager();

        container = new JBIContainer();
        container.setUseMBeanServer(false);
        container.setRootDir(rootDir.getAbsolutePath());
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
        activateComponent();
        container.start();

        container.deactivateComponent("ODE");
    }

    public void testProcessLifeCycle() throws Exception {
        activateComponent();
        container.start();

        component.getServiceUnitManager().deploy("HelloWorld", helloWorldDir);
        component.getServiceUnitManager().init("HelloWorld", helloWorldDir);
        component.getServiceUnitManager().start("HelloWorld");
        component.getServiceUnitManager().stop("HelloWorld");
        component.getServiceUnitManager().undeploy("HelloWorld", helloWorldDir);

        container.deactivateComponent("ODE");

        container.stop();
    }

    public void testHelloWorld() throws Exception {
        activateComponent();
        container.start();

        component.getServiceUnitManager().deploy("HelloWorld", helloWorldDir);
        component.getServiceUnitManager().init("HelloWorld", helloWorldDir);
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

    private void activateComponent() throws Exception {
        ComponentContextImpl cc = new ComponentContextImpl(container, new ComponentNameSpace(container.getName(), "ODE"));
        ActivationSpec activationSpec = new ActivationSpec();
        activationSpec.setComponent(component);
        activationSpec.setComponentName("ODE");
        container.activateComponent(odeDir, component, "", cc, activationSpec,  true, false, false, null);
    }

}
