package org.apache.ode.axis2.instancecleanup;

import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.hibernate.Query;

public class CleanSuccessHibTest extends CleanSuccessTest {
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
        Query query = HibDaoConnectionFactoryImpl.getSession().createQuery("select count(id) from HLargeData as l");

        return ((Long)query.uniqueResult()).intValue();
    }
}