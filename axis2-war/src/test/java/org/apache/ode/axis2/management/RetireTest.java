package org.apache.ode.axis2.management;

import org.apache.ode.axis2.Axis2TestBase;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class RetireTest extends Axis2TestBase {

    @Test(dataProvider="configs")
    public void testRetiredInstance() throws Exception {
        String bundleName = "TestInstanceRetire";
        System.out.println("=> " + server.getODEServer().getProcessStore().getPackages());
        if (server.isDeployed("1")) server.undeployProcess(bundleName + "/1");
        if (server.isDeployed("2")) server.undeployProcess(bundleName + "/2");
        
        QName deployedQName = server.deployProcess(bundleName + "/1").iterator().next();

        server.sendRequestFile("http://localhost:8888/processes/testretire",
                bundleName + "/1", "testRequest1.soap");

        server.getODEServer().getProcessManagement().setRetired(deployedQName, true);
        server.deployProcess(bundleName + "/2");
        
        String response = server.sendRequestFile("http://localhost:8888/processes/testretire",
                bundleName + "/1", "testRequest2.soap");

        System.out.println("###############################################");
        System.out.println(response);
        System.out.println("###############################################");
        assertTrue(response.indexOf("DONE") > 0);
    }
}
