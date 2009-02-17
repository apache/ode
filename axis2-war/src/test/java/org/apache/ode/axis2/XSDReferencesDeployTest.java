package org.apache.ode.axis2;

import org.testng.annotations.Test;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class XSDReferencesDeployTest extends Axis2TestBase {
    @Test(dataProvider="configs")
    public void testSimpleFaultCatch() throws Exception {
        server.deployService(DummyService.class.getCanonicalName());
        // If we have a bug, this will throw an exception, no need to assert
        server.deployProcess("XSDReferences");

        server.undeployProcess("XSDReferences");
    }
}
