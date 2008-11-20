package org.apache.ode.axis2.correlation;

import org.testng.annotations.BeforeMethod;

public class CorrelationMultiHibTest extends CorrelationMultiTest {
    @BeforeMethod
    protected void setUp() throws Exception {
    	System.setProperty("org.apache.ode.configDir", 
    			getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.hib");
        super.setUp();
    }
}
