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
package org.apache.ode.store;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessConf.CLEANUP_CATEGORY;

public class ProcessStoreTest extends TestCase {

    ProcessStoreImpl _ps;
    private File _testdd;

    public void setUp() throws Exception {
        System.setProperty("openjpa.properties", "/openjpa.xml");
        _ps = new ProcessStoreImpl();
        _ps.loadAll();
        URI tdd= getClass().getResource("/testdd/deploy.xml").toURI();
        _testdd = new File(tdd.getPath()).getParentFile();
    }

    public void tearDown() throws Exception {
        _ps.shutdown();
    }

    public void testSanity() {
        assertEquals(0,_ps.getProcesses().size());
        assertEquals(0,_ps.getPackages().size());
        assertNull(_ps.listProcesses("foobar"));
    }

    public void testDeploy() {
        Collection<QName> deployed = _ps.deploy(_testdd);
        assertNotNull(deployed);
        assertEquals(1,deployed.size());
    }

    public void testGetProcess() {
        Collection<QName> deployed = _ps.deploy(_testdd);
        QName pname = deployed.iterator().next();
        assertNotNull(deployed);
        assertEquals(1,deployed.size());
        ProcessConf pconf = _ps.getProcessConfiguration(pname);
        assertNotNull(pconf);
        assertEquals(_testdd.getName(),pconf.getPackage());
        assertEquals(pname, pconf.getProcessId());
    }

    public void testGetProcesses() {
        Collection<QName> deployed = _ps.deploy(_testdd);
        QName pname = deployed.iterator().next();
        assertNotNull(deployed);
        assertEquals(1,deployed.size());
        List<QName> pconfs = _ps.getProcesses();
        assertEquals(pname,pconfs.get(0));
    }

    public void testCleanupConfigurations() {
        Collection<QName> deployed = _ps.deploy(_testdd);
        QName pname = deployed.iterator().next();
        assertNotNull(deployed);
        assertEquals(1,deployed.size());
        ProcessConf pconf = _ps.getProcessConfiguration(pname);
        assertNotNull(pconf);
        assertEquals(_testdd.getName(),pconf.getPackage());
        assertEquals(pname, pconf.getProcessId());

        assertEquals(EnumSet.allOf(CLEANUP_CATEGORY.class), pconf.getCleanupCategories(true));
        assertEquals(EnumSet.of(CLEANUP_CATEGORY.MESSAGES, CLEANUP_CATEGORY.EVENTS), pconf.getCleanupCategories(false));

        assertTrue(pconf.isCleanupCategoryEnabled(true, CLEANUP_CATEGORY.INSTANCE));
        assertTrue(pconf.isCleanupCategoryEnabled(true, CLEANUP_CATEGORY.VARIABLES));
        assertTrue(pconf.isCleanupCategoryEnabled(true, CLEANUP_CATEGORY.MESSAGES));
        assertTrue(pconf.isCleanupCategoryEnabled(true, CLEANUP_CATEGORY.CORRELATIONS));
        assertTrue(pconf.isCleanupCategoryEnabled(true, CLEANUP_CATEGORY.EVENTS));
        assertFalse(pconf.isCleanupCategoryEnabled(false, CLEANUP_CATEGORY.INSTANCE));
        assertFalse(pconf.isCleanupCategoryEnabled(false, CLEANUP_CATEGORY.VARIABLES));
        assertTrue(pconf.isCleanupCategoryEnabled(false, CLEANUP_CATEGORY.MESSAGES));
        assertFalse(pconf.isCleanupCategoryEnabled(false, CLEANUP_CATEGORY.CORRELATIONS));
        assertTrue(pconf.isCleanupCategoryEnabled(false, CLEANUP_CATEGORY.EVENTS));

        assertEquals(2, pconf.getCronJobs().size());
        assertNotNull(pconf.getCronJobs().get(0).getCronExpression());
        assertEquals(3, pconf.getCronJobs().get(0).getRunnableDetailList().size());
    }
}
