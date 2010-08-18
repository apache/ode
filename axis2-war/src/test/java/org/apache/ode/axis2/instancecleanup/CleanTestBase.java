/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.axis2.instancecleanup;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;

import org.apache.ode.axis2.Axis2TestBase;
import org.apache.ode.axis2.ODEConfigDirAware;
import org.apache.ode.axis2.ODEConfigProperties;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ProcessInstanceProfileDAO;
import org.apache.ode.bpel.dao.ProcessProfileDAO;
import org.apache.ode.il.EmbeddedGeronimoFactory;
import org.apache.ode.il.dbutil.Database;
import org.testng.annotations.AfterMethod;

import javax.transaction.TransactionManager;

public abstract class CleanTestBase extends Axis2TestBase implements ODEConfigDirAware {
    protected ProfilingBpelDAOConnection daoConn;
    protected TransactionManager txm;
    protected int initialLargeDataCount = 0;

    @AfterMethod
    protected void tearDown() throws Exception {
        stopTM();
        super.tearDown();
    }

    protected void initTM() throws Exception {
        if( txm != null ) {
            try {
                txm.commit();
            } catch( Exception e ) {
                //ignore
            }
        }
        EmbeddedGeronimoFactory factory = new EmbeddedGeronimoFactory();
        txm = factory.getTransactionManager();
        Database db = getDatabase();
        db.setTransactionManager(txm);
        db.start();
        txm.begin();

        daoConn = (ProfilingBpelDAOConnection)db.createDaoCF().getConnection();
    }

    protected void stopTM() throws Exception {
        if( txm != null ) {
            try {
                txm.commit();
            } catch( Exception e ) {
                //ignore
            }
            txm = null;
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
        return txm;
    }

    protected ProcessDAO assertInstanceCleanup(int instances, int activityRecoveries, int correlationSets, int faults, int exchanges, int routes, int messsages, int partnerLinks, int scopes, int variables, int events, int largeData) throws Exception {
        initTM();
        ProcessInstanceProfileDAO profile = daoConn.createProcessInstanceProfile(getInstance());

        assertEquals("Number of instances", instances, profile.findInstancesByProcess().size());
        assertEquals("Number of activity recoveries", activityRecoveries, profile.findActivityRecoveriesByInstance().size());
        assertEquals("Number of correlation sets", correlationSets, profile.findCorrelationSetsByInstance().size());
        assertEquals("Number of faults", faults, profile.findFaultsByInstance().size());
        assertEquals("Number of message exchanges", exchanges, profile.findMessageExchangesByInstance().size());
        assertEquals("Number of message routes", routes, profile.findMessageRoutesByInstance().size());
        assertEquals("Number of messages", messsages, profile.findMessagesByInstance().size());
        assertEquals("Number of partner links", partnerLinks, profile.findPartnerLinksByInstance().size());
        assertEquals("Number of scopes", scopes, profile.findScopesByInstance().size());
        assertEquals("Number of variables", variables, profile.findXmlDataByInstance().size());
        assertEquals("Number of events", events, profile.countEventsByInstance());
        assertEquals("Number of large data", largeData, getLargeDataCount(largeData) - initialLargeDataCount);

        return profile.getProcess();
    }

    protected void assertProcessCleanup(ProcessDAO process) throws Exception {
        if( process != null ) {
            initTM();
            ProcessProfileDAO profile = daoConn.createProcessProfile(process);
            assertTrue("Process should have been deleted.", !profile.doesProcessExist());
            assertEquals("Number of instances", 0, profile.findInstancesByProcess().size());
            assertEquals("Number of activity recoveries", 0, profile.findActivityRecoveriesByProcess().size());
            assertEquals("Number of correlation sets", 0, profile.findCorrelationSetsByProcess().size());
            assertEquals("Number of correlators", 0, profile.findCorrelatorsByProcess().size());
            assertEquals("Number of faults", 0, profile.findFaultsByProcess().size());
            assertEquals("Number of message exchanges", 0, profile.findMessageExchangesByProcess().size());
            assertEquals("Number of message routes", 0, profile.findMessageRoutesByProcess().size());
            assertEquals("Number of messages", 0, profile.findMessagesByProcess().size());
            assertEquals("Number of partner links", 0, profile.findPartnerLinksByProcess().size());
            assertEquals("Number of scopes", 0, profile.findScopesByProcess().size());
            assertEquals("Number of variables", 0, profile.findXmlDataByProcess().size());
            assertEquals("Number of events", 0, profile.countEventsByProcess());
            assertEquals("Number of large data", 0, getLargeDataCount(0) - initialLargeDataCount);
        }
    }

    protected abstract ProcessInstanceDAO getInstance();

    protected int getLargeDataCount(int echoCount) throws Exception {
        return echoCount;
    }

}