package org.apache.ode.store;

import java.io.File;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

/**
 * Test for {@link org.apache.ode.store.DeploymentUnitDir}. 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class DeploymentUnitTest  extends TestCase {
    DeploymentUnitDir du;
    
    public void setUp() throws Exception {
        File dir = new File(getClass().getResource("/complexImport/deploy.xml").getPath()).getParentFile();
        du = new DeploymentUnitDir(dir);
    }
    
    public void testRegistry() {
        DocumentRegistry dr = du.getDocRegistry();
        assertNotNull(dr.getDefinitionForPortType(new QName("http://ode/bpel/unit-test.wsdl","HelloPortType")));
    }
    
    public void testCompile() {
        du.compile();
    }
    
}
