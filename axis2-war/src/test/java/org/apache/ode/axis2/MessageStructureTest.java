package org.apache.ode.axis2;

import org.apache.ode.utils.DOMUtils;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

public class MessageStructureTest extends Axis2TestBase {
    @Test(dataProvider="configs")
    public void testAttrWithNsValue() throws Exception {
        String bundleName = "TestAttributeNamespaces";
        // deploy the required service
        server.deployService(DummyService.class.getCanonicalName());
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        try {
            String response = server.sendRequestFile("http://localhost:8888/processes/attrNSWorld",
                    bundleName, "testRequest.soap");
            Element domResponse = DOMUtils.stringToDOM(response);
            Element out = DOMUtils.getFirstChildElement(DOMUtils.getFirstChildElement(DOMUtils.getFirstChildElement(domResponse)));
            String nsAttr = out.getAttribute("xmlns:myns");
            System.out.println("=> " + response);
            assertTrue(nsAttr != null);
        } finally {
            server.undeployProcess(bundleName);
        }
    }
}
