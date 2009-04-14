package org.apache.ode.axis2;

import static org.testng.AssertJUnit.*;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import java.util.regex.Pattern;

/**
 * Tests that when a process is called and some error happens during processing, the error is communicated asap back to the client.<br/>
 * The client should NOT get a timeout exception which would be confusing.
 * <p>
 * The test consists in instantiating a process that will try to invoke an dummy service. But the dummy service does NOT exist. So a failure will occur.
 * The client should get that failure back and not a TimeoutException. 
 */
public class FailFastTest extends Axis2TestBase {


    @DataProvider(name = "input")
    private Object[][] bundleLIst(){
        return new Object[][]{
                {"TestFailFast/invoke", ".*Message exchange failure due to: The service cannot be found for the endpoint reference .*"},
                {"TestFailFast/faultOnFailure", ".*xmlns:axis2ns\\d=\"http://ode.apache.org/activityRecovery\">axis2ns\\d:activityFailure.*"},
                {"TestFailFast/selectionFailure", ".*xmlns:axis2ns\\d=\"http://docs.oasis-open.org/wsbpel/2.0/process/executable\">axis2ns\\d:selectionFailure.*"}
        };
    }

    @Test(dataProvider="input")
    public void shouldNotTimeout(String bundleName, String expectedMsgPattern) throws Exception {
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/helloWorld", bundleName, "testRequest.soap");
            System.out.println(response);
            String badMessage = "java.util.concurrent.TimeoutException: Message exchange";
            assertFalse("Client should NOT time out! It should receive the true failure", response.contains(badMessage));
            assertTrue("Client did not receive the right error message!", Pattern.compile(expectedMsgPattern, Pattern.DOTALL).matcher(response).matches());
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}