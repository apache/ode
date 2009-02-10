package org.apache.ode.axis2;

import static org.testng.Assert.fail;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

public class BpelActivityTest extends Axis2TestBase implements ODEConfigDirAware {
	public String getODEConfigDir() {
		return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.hib-derby";
	}

//	@Test(dataProvider="configs")
    public void testSimpleFaultCatch() throws Exception {
		final String bundleName = "TestThrowOnEventNoCatch";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);

        new Thread() {
        	public void run() {
        		try {
        			Thread.sleep(3000);
        			String response = server.sendRequestFile("http://localhost:8888/ode/processes/NPG072408_4/test4/process/Pool",
                            bundleName, "testRequest2.soap");
        			Logger.getLogger(BpelActivityTest.class).debug("!!!SEAN : " + response);
        		} catch( Exception e ) {
        			fail(e.getMessage());
        		}
        	}
        }.start();

        new Thread() {
        	public void run() {
        		try {
        			Thread.sleep(6000);
        			String response = server.sendRequestFile("http://localhost:8888/ode/processes/NPG072408_4/test4/process/Pool",
                            bundleName, "testRequest3.soap");
        			Logger.getLogger(BpelActivityTest.class).debug("!!!SEAN : " + response);
        		} catch( Exception e ) {
        			fail(e.getMessage());
        		}
        	}
        }.start();

        try {
            String response = server.sendRequestFile("http://localhost:8888/ode/processes/NPG072408_4/test4/process/Pool",
                    bundleName, "testRequest.soap");
            Logger.getLogger(BpelActivityTest.class).debug("!!!SEAN : " + response);
            
//            assertTrue(response.contains("helloResponse") && response.contains("Something went wrong. Fortunately, it was meant to be."));
        } finally {
//            server.undeployProcess(bundleName);
        }
        
        Thread.sleep(6000);
    }
}
