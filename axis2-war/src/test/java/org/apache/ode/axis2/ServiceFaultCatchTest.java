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
        String bundleName = "TestStructuredFault";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        // Deploy the process if necessary.
        // Actually undeployed process are automatically re-deployed during ODE Server start-up.
        if(!server._ode.getProcessStore().getPackages().contains(bundleName)){
            server.deployProcess(bundleName);
        }
        try {
            String response = server.sendRequestFile("http://localhost:8080/processes/helloWorld",
                    bundleName, "testRequest.soap");

            assert(response.indexOf("Something went wrong. Fortunately, it was meant to be.") >= 0);
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}
