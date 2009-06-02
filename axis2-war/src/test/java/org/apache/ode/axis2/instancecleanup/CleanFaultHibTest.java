package org.apache.ode.axis2.instancecleanup;

import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.hibernate.Query;

public class CleanFaultHibTest extends CleanFaultTest {
    @Override
    public String getODEConfigDir() {
        return HIB_DERBY_CONF_DIR;
    }
    
    @Override
    protected ProcessInstanceDAO getInstance() {
        return HibDaoConnectionFactoryImpl.getInstance();
    }

    @Override
    protected int getLargeDataCount(int echoCount) throws Exception {
        initTM();
//        LogFactory.getLog(CleanFailureHibTest.class).debug("LARGE_DATA left over: " + HibDaoConnectionFactoryImpl.getSession().createQuery("from HLargeData as l").list());
        Query query = HibDaoConnectionFactoryImpl.getSession().createQuery("select count(id) from HLargeData as l");
        
        return ((Long)query.uniqueResult()).intValue();
    }
}