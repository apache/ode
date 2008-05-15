package org.apache.ode.axis2;

public class TestSimpleScenario extends Axis2TestBase {

    protected void setUp() throws Exception {
        start();
    }
    protected void tearDown() throws Exception {
        server.stop();
    }
    

    public void testDynPartner() throws Exception {
        String bundleName = "TestDynPartner";
        if(!server._ode.getProcessStore().getPackages().contains(bundleName)){
            server.deployProcess(bundleName);
        }
        try {
            String response = server.sendRequestFile("http://localhost:8080/ode/processes/DynMainService",
                    bundleName, "testRequest.soap");

            assertTrue(response.indexOf("OK") > 0);
        } finally {
            server.undeployProcess(bundleName);
        }

    }

    public void testMagicSession() throws Exception {
        String bundleName = "TestMagicSession";
        if(!server._ode.getProcessStore().getPackages().contains(bundleName)){
            server.deployProcess(bundleName);
        }
        try {
            String response = server.sendRequestFile("http://localhost:8080/ode/processes/MSMainExecuteService",
                    bundleName, "testRequest.soap");

            System.out.println("->" + response);
            assertTrue(response.indexOf("OK") > 0);
        } finally {
            server.undeployProcess(bundleName);
        }

    }
}
