package org.apache.ode.jbi;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.ode.utils.DOMUtils;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.objectweb.jotm.Jotm;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

public class TestOdeJbiComponentLifeCycle extends TestCase {

    JBIContainer container;
    File rootDir, installDir, odeDir;
    Jotm jotm;
    private OdeComponent component;

    protected void setUp() throws Exception {
        rootDir = File.createTempFile("smixInstallDir","");
        installDir = new File(rootDir,"install");
        installDir.mkdir();
        jotm = new Jotm(true,false);
        odeDir = new File(installDir,"ODE");
        odeDir.mkdir();
        
        container = new JBIContainer();
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.setEmbedded(true);
        
        
        container.setTransactionManager(jotm.getTransactionManager());
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
        component.getServiceUnitManager().deploy("HelloWorld", "../jbi-examples/src/examples/HelloWorld2/HelloWorld2-process");
        component.getServiceUnitManager().init("HelloWorld", "../jbi-examples/src/examples/HelloWorld2/HelloWorld2-process");
        component.getServiceUnitManager().start("HelloWorld");
        component.getServiceUnitManager().stop("HelloWorld");
        component.getServiceUnitManager().undeploy("HelloWorld", "../jbi-examples/src/examples/HelloWorld2/HelloWorld2-process");

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
