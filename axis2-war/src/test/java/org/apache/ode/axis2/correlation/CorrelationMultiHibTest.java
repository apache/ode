package org.apache.ode.axis2.correlation;

public class CorrelationMultiHibTest extends CorrelationMultiTest {
    @Override
    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.hib-derby"; 
    }
}