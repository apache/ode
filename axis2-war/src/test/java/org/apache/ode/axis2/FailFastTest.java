package org.apache.ode.axis2;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;

/**
 * Tests that when a process is called and some error happens during processing, the error is communicated asap back to the client.<br/>
 * The client should NOT get a timeout exception which would be confusing.
 * <p>
 * The test consists in instantiating a process that will try to invoke an dummy service. But the dummy service does NOT exist. So a failure will occur.
 * The client should get that failure back and not a TimeoutException. 
 */
public class FailFastTest extends Axis2TestBase {
    @Test(dataProvider="configs")
    public void gimmeTheDamnFailure() throws Exception {
        String bundleName = "TestFailFast";
        // Intentionnally NOT deploy the required service, to make the invocation fail
//        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld",
                    bundleName, "testRequest.soap");
            System.out.println(response);
            String badBadMessage = "java.util.concurrent.TimeoutException: Message exchange org.apache.ode.bpel.engine.MyRoleMessageExchangeImpl$ResponseFuture";
            assertFalse("Client should NOT time out! It should receive the true failure", response.contains(badBadMessage));
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}