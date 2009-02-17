package org.apache.ode.axis2.instancecleanup;

import org.apache.ode.axis2.DummyService;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.testng.annotations.Test;

public class CleanFailureTest extends CleanTestBase {
    @Test(dataProvider="configs")
    public void testCleanAll() throws Exception {
        String bundleName = "TestCleanFailure";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            initialLargeDataCount = getLargeDataCount(0);
            server.sendRequestFile("http://localhost:8888/processes/helloWorld", bundleName, "testRequest.soap");
        } finally {
            ProcessDAO process = getProcess();
            server.undeployProcess(bundleName);
            assertProcessCleanup(process);
        }
    }

    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.jpa-derby";
    }
    
    protected ProcessInstanceDAO getInstance() {
        return JpaDaoConnectionFactoryImpl.getInstance();
    }

    protected ProcessDAO getProcess() {
        return JpaDaoConnectionFactoryImpl.getProcess();
    }

    @Override
    protected int getLargeDataCount(int echoCount) throws Exception {
        return echoCount;
    }
}