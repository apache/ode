package org.apache.ode.axis2;

/**
 * Tests that a fault thrown by a called service can be caught and is properly
 * structured so that an assign on a fault sub-element will succeed.
 *
 * @author Matthieu Riou <mriou@apache.org>
 */
public class ServiceFaultCatchTest extends Axis2TestBase {
	public ServiceFaultCatchTest(String name) {
		super(name);
	}
	
    public void testSimpleFaultCatch() throws Exception {
        String bundleName = "TestStructuredFault";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (!server.isDeployed(bundleName)) server.deployProcess(bundleName);

        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld",
                    bundleName, "testRequest.soap");

            assertTrue(response.indexOf("Something went wrong. Fortunately, it was meant to be.") >= 0);
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}
