package org.apache.ode.axis2.instancecleanup;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.ODEConfigDirAware;
import org.apache.ode.axis2.ODEConfigProperties;
import org.apache.ode.dao.bpel.ProcessDAO;
import org.apache.ode.dao.bpel.ProcessInstanceDAO;
import org.apache.ode.dao.bpel.ProcessInstanceProfileDAO;
import org.apache.ode.dao.bpel.ProcessProfileDAO;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.il.dbutil.Database;
import org.testng.annotations.AfterMethod;

import javax.transaction.TransactionManager;

public abstract class CleanTestBase extends Axis2TestBase implements ODEConfigDirAware {
    protected ProfilingBpelDAOConnection daoConn;
    protected TransactionManager _txm;
    protected int initialLargeDataCount = 0;
    
    @AfterMethod
    protected void tearDown() throws Exception {
        stopTM();
        super.tearDown();
    }
    
    protected void initTM() throws Exception {
        if( _txm != null ) {
            try {
                _txm.commit();
            } catch( Exception e ) {
                //ignore 
            }
        }
        _txm = new EmbeddedGeronimoFactory().getTransactionManager();
        Database db = getDatabase();
        db.setTransactionManager(_txm);
        db.start();
        _txm.begin();

        daoConn = (ProfilingBpelDAOConnection)db.createDaoCF().getConnection();
    }

    protected void stopTM() throws Exception {
        if( _txm != null ) {
            try {
                _txm.commit();
            } catch( Exception e ) { 
                //ignore 
            }
            _txm = null;
        }
    }

    protected Database getDatabase() throws Exception {
        String odeConfigDir = getODEConfigDir();
        if( config == null || DO_NOT_OVERRIDE_CONFIG.equals(config) || "<jpa>".equals(config) || "<hib>".equals(config) ) {
            System.out.println("Profiling config, default: " + odeConfigDir);
        } else {
            System.out.println("Profiling config: " + config + ".");
            odeConfigDir = config;
        }
        File configFile = new File(odeConfigDir);
        ODEConfigProperties odeProps = new ODEConfigProperties(configFile);
        odeProps.load();
        Database db = new Database(odeProps);
        String webappPath = getClass().getClassLoader().getResource("webapp").getFile();
        db.setWorkRoot(new File(webappPath, "/WEB-INF"));
        
        return db;
    }

    protected TransactionManager getTransactionManager() {
        return _txm;
    }

    protected ProcessDAO assertInstanceCleanup(int instances, int activityRecoveries, int correlationSets, int faults, int exchanges, int routes, int messsages, int partnerLinks, int scopes, int variables, int events, int largeData) throws Exception {
        initTM();
        ProcessInstanceProfileDAO profile = daoConn.createProcessInstanceProfile(getInstance());

        assertZeroOrNonZero("Number of instances", instances, profile.findInstancesByProcess().size());
        assertZeroOrNonZero("Number of activity recoveries", activityRecoveries, profile.findActivityRecoveriesByInstance().size());
        assertZeroOrNonZero("Number of correlation sets", correlationSets, profile.findCorrelationSetsByInstance().size());
        assertZeroOrNonZero("Number of faults", faults, profile.findFaultsByInstance().size());
        assertZeroOrNonZero("Number of message exchanges", exchanges, profile.findMessageExchangesByInstance().size());
        assertZeroOrNonZero("Number of message routes", routes, profile.findMessageRoutesByInstance().size());
        assertZeroOrNonZero("Number of messages", messsages, profile.findMessagesByInstance().size());
        assertZeroOrNonZero("Number of partner links", partnerLinks, profile.findPartnerLinksByInstance().size());
        assertZeroOrNonZero("Number of scopes", scopes, profile.findScopesByInstance().size());
        assertZeroOrNonZero("Number of variables", variables, profile.findXmlDataByInstance().size());
        assertZeroOrNonZero("Number of events", events, profile.countEventsByInstance());
        assertZeroOrNonZero("Number of large data", largeData, getLargeDataCount(largeData) - initialLargeDataCount);

        return profile.getProcess();
    }

    protected void assertProcessCleanup(ProcessDAO process) throws Exception {
        if( process != null ) {
            initTM();
            ProcessProfileDAO profile = daoConn.createProcessProfile(process);
            assertTrue("Process should have been deleted.", !profile.doesProcessExist());
            assertZeroOrNonZero("Number of instances", 0, profile.findInstancesByProcess().size());
            assertZeroOrNonZero("Number of activity recoveries", 0, profile.findActivityRecoveriesByProcess().size());
            assertZeroOrNonZero("Number of correlation sets", 0, profile.findCorrelationSetsByProcess().size());
            assertZeroOrNonZero("Number of correlators", 0, profile.findCorrelatorsByProcess().size());
            assertZeroOrNonZero("Number of faults", 0, profile.findFaultsByProcess().size());
            assertZeroOrNonZero("Number of message exchanges", 0, profile.findMessageExchangesByProcess().size());
            assertZeroOrNonZero("Number of message routes", 0, profile.findMessageRoutesByProcess().size());
            assertZeroOrNonZero("Number of messages", 0, profile.findMessagesByProcess().size());
            assertZeroOrNonZero("Number of partner links", 0, profile.findPartnerLinksByProcess().size());
            assertZeroOrNonZero("Number of scopes", 0, profile.findScopesByProcess().size());
            assertZeroOrNonZero("Number of variables", 0, profile.findXmlDataByProcess().size());
            assertZeroOrNonZero("Number of events", 0, profile.countEventsByProcess());
            assertZeroOrNonZero("Number of large data", 0, getLargeDataCount(0) - initialLargeDataCount);
        }
    }

    protected void assertZeroOrNonZero(String message, int expected, int actual) {
        // It seems we are generating different number of objects between OpenJPA and Hibernate on ODE trunk.
        if( expected == 0 ) {
            assertEquals(message, expected, actual);
        } else {
            assertTrue(message + " should be bigger than 0", actual > 0);
        }
    }
    protected abstract ProcessInstanceDAO getInstance();
    
    protected abstract int getLargeDataCount(int echoCount) throws Exception;
}