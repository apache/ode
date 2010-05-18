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

package org.apache.ode.test;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.iapi.ProcessState;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
public class VersionedRedeployTest extends BPELTestAbstract {

    QName qName1 = new QName("http://ode/bpel/unit-test", "HelloWorld2-1");
    QName qName2 = new QName("http://ode/bpel/unit-test", "HelloWorld2-2");
    QName qName3 = new QName("http://ode/bpel/unit-test", "HelloWorld2-3");

    @Test public void testRetireOld() throws Throwable {
        deploy("/bpel/2.0/TestVersionedRedeploy/HelloWorld-1");
        ProcessConf conf = store.getProcessConfiguration(qName1);
        Assert.assertEquals(ProcessState.ACTIVE, conf.getState());

        deploy("/bpel/2.0/TestVersionedRedeploy/HelloWorld-2");

        // Now 1 should be retired and 2 active
        conf = store.getProcessConfiguration(qName1);
        Assert.assertEquals(ProcessState.RETIRED, conf.getState());
        conf = store.getProcessConfiguration(qName2);
        Assert.assertEquals(ProcessState.ACTIVE, conf.getState());

        deploy("/bpel/2.0/TestVersionedRedeploy/HelloWorld-3");

        // 1 and 2 should be retired and 3 active
        conf = store.getProcessConfiguration(qName1);
        Assert.assertEquals(ProcessState.RETIRED, conf.getState());
        conf = store.getProcessConfiguration(qName2);
        Assert.assertEquals(ProcessState.RETIRED, conf.getState());
        conf = store.getProcessConfiguration(qName3);
        Assert.assertEquals(ProcessState.ACTIVE, conf.getState());
    }

    @Test public void testInstancePersistence() throws Throwable {
        // Checking for each step that all instances still exist and that each process got one execution
        // so no instance has been created after a process has been retired.
        go("/bpel/2.0/TestVersionedRedeploy/HelloWorld-1");
        Assert.assertEquals(1, _cf.getConnection().getProcess(qName1).getNumInstances());

        // clean up deployment and invocations
        _deployments.clear();
        _invocations.clear();
        
        go("/bpel/2.0/TestVersionedRedeploy/HelloWorld-2");
        Assert.assertEquals(1, _cf.getConnection().getProcess(qName1).getNumInstances());
        Assert.assertEquals(1, _cf.getConnection().getProcess(qName2).getNumInstances());
        
        // clean up deployment and invocations
        _deployments.clear();
        _invocations.clear();
        
        go("/bpel/2.0/TestVersionedRedeploy/HelloWorld-3");
        Assert.assertEquals(1, _cf.getConnection().getProcess(qName1).getNumInstances());
        Assert.assertEquals(1, _cf.getConnection().getProcess(qName2).getNumInstances());
        Assert.assertEquals(1, _cf.getConnection().getProcess(qName3).getNumInstances());
    }

    @Test public void testVersionedUndeployDeploy() throws Throwable {
        go("/bpel/2.0/TestVersionedRedeploy/HelloWorld-1");
        doUndeployments();

        //clean up invocations before next run
        _invocations.clear();

        go("/bpel/2.0/TestVersionedRedeploy/HelloWorld-1");
        // We should have a brand new version 1 with no version 2
        Assert.assertNull(store.getProcessConfiguration(qName1));
        Assert.assertNull(store.getProcessConfiguration(qName3));
        
        Assert.assertEquals(1, _cf.getConnection().getProcess(qName2).getNumInstances());
    }

}
