package org.apache.ode.axis2.instancecleanup;

import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.hibernate.Query;

public class CleanFailureHibTest extends CleanFailureTest {
    @Override
    public String getODEConfigDir() {
        return getClass().getClassLoader().getResource("webapp").getFile() + "/WEB-INF/conf.hib-derby";
    }

    @Override
    protected ProcessInstanceDAO getInstance() {
        return HibDaoConnectionFactoryImpl.getInstance();
    }

    @Override
    protected ProcessDAO getProcess() {
        return HibDaoConnectionFactoryImpl.getProcess();
    }

    @Override
    protected int getLargeDataCount(int echoCount) throws Exception {
        initTM();
        Query query = HibDaoConnectionFactoryImpl.getSession().createQuery("select count(id) from HLargeData as l");
        
        return ((Long)query.uniqueResult()).intValue();
    }
}