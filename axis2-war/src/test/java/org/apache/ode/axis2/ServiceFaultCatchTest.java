package org.apache.ode.axis2;

import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;

import java.net.URL;
import java.io.FileInputStream;

/**
 * Tests that a fault thrown by a called service can be caught and is properly
 * structured so that an assign on a fault sub-element will succeed.
 * @author Matthieu Riou <mriou@apache.org>
 */
public class ServiceFaultCatchTest extends Axis2TestBase {

    protected void setUp() throws Exception {
        start();
    }

    public void testSimpleFaultCatch() throws Exception {
        server.deployService(DummyService.class.getCanonicalName());
        server.deployProcess("TestStructuredFault");

        String response = server.sendRequestFile("http://localhost:8080/processes/helloWorld",
                "TestStructuredFault", "testRequest.soap");
        
        assert(response.indexOf("Something went wrong. Fortunately, it was meant to be.") >= 0);

        server.undeployProcess("TestStructuredFault");
    }
}
