package org.apache.ode.axis2.httpbinding;

import org.apache.ode.axis2.Axis2TestBase;

import java.util.concurrent.CountDownLatch;

/**
 * <p/>
 * This unit test passes an integer to a BPEL. Then the BPEL invokes the 6 operations of Arithmetics.wsdl.
 * These operations are set up to use the various Http binding configurations.  
 * <p/>
 * From a "business" standpoint:<br/>
 * Let N be the input number, stored in the testRequest.soap file<br/>
 * This test will compute the Sum of the first (N + 300) positive integers.
 * <p/>
 * If N=10, the expected result is 310*(310+1)/2 = 48205
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class HttpBindingTest extends Axis2TestBase {
    protected ArithmeticsJettyWrapper jettyWrapper;

    CountDownLatch latch;

    protected void setUp() throws Exception {
        super.setUp();
        latch = new CountDownLatch(1);
        jettyWrapper = new ArithmeticsJettyWrapper(7070);
        new Thread("HttpBindingJetty") {
            public void run() {
                try {
                    jettyWrapper.server.start();
                    latch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    protected void tearDown() throws Exception {
        jettyWrapper.server.stop();
        super.tearDown();
    }

    public void testHttpBinding() throws Exception {
        // wait for jetty to be ready
        latch.await();
        String bundleName = "TestHttpBinding";
        // deploy the required service
        if (!server.isDeployed(bundleName)) server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8080/processes/helloWorld",
                    bundleName, "testRequest.soap");
            int valueInSoapRequest = 10;
            int n = 300 + valueInSoapRequest;
            assertTrue(response.indexOf(String.valueOf(n * (n + 1) / 2)) >= 0);
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}