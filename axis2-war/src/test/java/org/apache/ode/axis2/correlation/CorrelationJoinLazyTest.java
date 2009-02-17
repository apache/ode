package org.apache.ode.axis2.correlation;

import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.DummyService;
import org.apache.ode.axis2.ODEConfigDirAware;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class CorrelationJoinLazyTest extends Axis2TestBase implements ODEConfigDirAware {
    /**
     * Tests a message being saved by no instance waiting for it. The saved message is picked up
     * when the third message arrives, and is consumed.
     * 
     * @throws Exception
     */
    @Test(dataProvider="configs")
    public void testCorrelationJoin() throws Exception {
        final String bundleName = "TestCorrelationJoinLazy";
        
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(3000);
                    server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                            bundleName, "testRequest2.soap");
                } catch( Exception e ) {
                    fail(e.getMessage());
                }
            }
        }.start();
        
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(6000);
                    server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                            bundleName, "testRequest3.soap");
                } catch( Exception e ) {
                    fail(e.getMessage());
                }
            }
        }.start();
        
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/correlationMultiTest",
                    bundleName, "testRequest.soap");
            System.out.println("=>\n" + response);
            assertTrue(response.contains(">1;3;2;<"));
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            server.undeployProcess(bundleName);
        }
    }

    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.jpa-derby"; 
    }
}