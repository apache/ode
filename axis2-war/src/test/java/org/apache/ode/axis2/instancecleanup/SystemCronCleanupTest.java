package org.apache.ode.axis2.instancecleanup;

import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.engine.cron.SystemSchedulesConfig;
import org.hibernate.Query;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SystemCronCleanupTest extends CleanTestBase {
    static {
        String customSchedulesFilePath = SystemCronCleanupTest.class.getClassLoader().getResource("webapp").getFile() + "/WEB-INF/test-schedules.xml";
        System.setProperty(SystemSchedulesConfig.SCHEDULE_CONFIG_FILE_PROP_KEY, customSchedulesFilePath);
    }

    // SEAN - Disable for now, wait until Alexis upgrades Axis to 1.5
//    @Test(dataProvider="configs")
    public void _testCleanAll() throws Exception {
        go("TestSystemCronCleanup", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

//    @Test(dataProvider="configs")
    public void _testCleanExclude() throws Exception {
        go("TestSystemCronCleanup_exclude", 1, 0, 0, 0, 3, 0, 6, 2, 3, 6, 59, 76);
    }

    @BeforeClass
    public void setCustomCronSchedules() {
        String customSchedulesFilePath = SystemCronCleanupTest.class.getClassLoader().getResource("webapp").getFile() + "/WEB-INF/test-schedules.xml";
        System.setProperty(SystemSchedulesConfig.SCHEDULE_CONFIG_FILE_PROP_KEY, customSchedulesFilePath);
    }
    
    @AfterClass
    public void resetCustomCronSchedules() {
        System.getProperties().remove(SystemSchedulesConfig.SCHEDULE_CONFIG_FILE_PROP_KEY);
    }
    
    protected void go(String bundleName, int instances, int activityRecoveries, int correlationSets, int faults, int exchanges, int routes, int messsages, int partnerLinks, int scopes, int variables, int events, int largeData) throws Exception {
        if (server.isDeployed(bundleName)) server.undeployProcess(bundleName);
        server.deployProcess(bundleName);
        ProcessDAO process = null;
        try {
            initialLargeDataCount = getLargeDataCount(0);
            
            server.sendRequestFile("http://localhost:8888/processes/FirstProcess/FirstProcess/FirstProcess/Client", bundleName, "testRequest.soap");
            // every second, clean up cron job kicks in
            Thread.sleep(2000);
            LogFactory.getLog(SystemCronCleanupTest.class).debug("============ASSERT INSTANCE CLEANUP===============");
            process = assertInstanceCleanup(instances, activityRecoveries, correlationSets, faults, exchanges, routes, messsages, partnerLinks, scopes, variables, events, largeData);
        } finally {
            server.undeployProcess(bundleName);
            assertProcessCleanup(process);
        }
    }

    public String getODEConfigDir() {
        return HIB_DERBY_CONF_DIR;
    }
    
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
