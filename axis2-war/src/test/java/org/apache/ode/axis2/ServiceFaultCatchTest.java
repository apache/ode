package org.apache.ode.axis2;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

/**
 * Tests that a fault thrown by a called service can be caught and is properly
 * structured so that an assign on a fault sub-element will succeed.
 *
 * @author Matthieu Riou <mriou@apache.org>
 */
public class ServiceFaultCatchTest extends Axis2TestBase {
    @Test(dataProvider="configs")
    public void testSimpleFaultCatch() throws Exception {
        String bundleName = "TestStructuredFault";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld",
                    bundleName, "testRequest.soap");
            assertTrue(response.contains("helloResponse") && response.contains("Something went wrong. Fortunately, it was meant to be."));
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}
