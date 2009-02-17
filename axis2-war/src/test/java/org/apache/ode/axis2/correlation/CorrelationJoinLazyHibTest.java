package org.apache.ode.axis2.correlation;

public class CorrelationJoinLazyHibTest extends CorrelationJoinLazyTest {
    @Override
    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.hib-derby"; 
    }
}