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
package org.apache.ode.bpel.runtime;

import junit.framework.TestCase;
import org.apache.ode.bpel.engine.BpelManagementFacadeImpl;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.pmapi.ProcessInfoCustomizer;
import org.apache.ode.bpel.pmapi.TProcessInfo;
import org.apache.ode.utils.DOMUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Test activity recovery and failure handling.
 */
public class ProcessManagementTest extends TestCase {

    static final String   NAMESPACE = "http://ode.apache.org/bpel/unit-test";
    MockBpelServer        _server;
    BpelManagementFacade  _management;
    QName                 _processQName;

  
    public void testFilterProcessesByName() throws Exception {

        List<TProcessInfo> pilist = _management.listProcesses(null,null).getProcessInfoList().getProcessInfoList();
        assertEquals(6,pilist.size());

        pilist = _management.listProcesses("name=FailureInh*",null).getProcessInfoList().getProcessInfoList();
        assertEquals(1,pilist.size());
        
        pilist = _management.listProcesses("name=FailureToRecovery*",null).getProcessInfoList().getProcessInfoList();
        assertEquals(1,pilist.size());
        
        pilist = _management.listProcesses("name=foobaz*",null).getProcessInfoList().getProcessInfoList();
        assertEquals(0,pilist.size());

        pilist = _management.listProcesses("namespace="+NAMESPACE,null).getProcessInfoList().getProcessInfoList();
        assertEquals(6,pilist.size());

        pilist = _management.listProcesses("namespace=http:*",null).getProcessInfoList().getProcessInfoList();
        assertEquals(6,pilist.size());
        
        pilist = _management.listProcesses("namespace=foo:*",null).getProcessInfoList().getProcessInfoList();
        assertEquals(0,pilist.size());
    }

    public void testListProcessesOrder() {
        List<TProcessInfo> pilist = 
            _management.listProcesses(null,"name").getProcessInfoList().getProcessInfoList();

        for (int i = 1 ; i <  pilist.size(); ++i) {
            QName qname = QName.valueOf(pilist.get(i).getPid());
            QName qnamePrev = QName.valueOf(pilist.get(i-1).getPid());
            assertTrue(0<=qname.getLocalPart().compareTo(qnamePrev.getLocalPart()));
        }

        pilist = 
            _management.listProcesses(null,"-name").getProcessInfoList().getProcessInfoList();

        for (int i = 1 ; i <  pilist.size(); ++i) {
            QName qname = QName.valueOf(pilist.get(i).getPid());
            QName qnamePrev = QName.valueOf(pilist.get(i-1).getPid());
            assertTrue(0>=qname.getLocalPart().compareTo(qnamePrev.getLocalPart()));
        }
    }
    
    public void testListProcessCustom() {
        List<TProcessInfo> pilist = 
            _management.listProcessesCustom(null,"name", ProcessInfoCustomizer.ALL).getProcessInfoList().getProcessInfoList();
        assertEquals(6,pilist.size());
    }
    
    protected void setUp() throws Exception {
        
        _server = new MockBpelServer();
        _server.deploy(new File(new URI(this.getClass().getResource("/recovery").toString())));
        _management = new BpelManagementFacadeImpl(_server._server,_server._store);
        execute("FailureInheritence");
    }

    protected void tearDown() throws Exception {
        _management.delete(null);
        _server.shutdown();
        _server = null;
        _management = null;
        _processQName = null;
    }

    /**
     * Call this to execute the process so it fails the specified number of times.
     * Returns when the process has either completed, or waiting for recovery to happen.
     */
    protected void execute(String process) throws Exception {
        _management.delete(null);
        _processQName = new QName(NAMESPACE, process);
        _server.invoke(_processQName, "instantiate",
                       DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:RequestElement"));
        _server.waitForBlocking();
    }
}
