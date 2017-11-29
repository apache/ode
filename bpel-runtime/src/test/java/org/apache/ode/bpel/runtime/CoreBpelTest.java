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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.iapi.ProcessConf.PartnerRoleConfig;
import org.apache.ode.bpel.obj.OCatch;
import org.apache.ode.bpel.obj.OEmpty;
import org.apache.ode.bpel.obj.OFaultHandler;
import org.apache.ode.bpel.obj.OFlow;
import org.apache.ode.bpel.obj.OMessageVarType;
import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.bpel.obj.OPartnerLink;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.bpel.obj.OScope.Variable;
import org.apache.ode.bpel.obj.OSequence;
import org.apache.ode.bpel.obj.OThrow;
import org.apache.ode.bpel.runtime.channels.ActivityRecovery;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.InvokeResponse;
import org.apache.ode.bpel.runtime.channels.PickResponse;
import org.apache.ode.bpel.runtime.channels.TimerResponse;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test core BPEL processing capabilities.
 */
public class CoreBpelTest extends TestCase implements BpelRuntimeContext {
    private boolean _completedOk;

    private boolean _terminate;

    private FaultData _fault;

    private ExecutionQueueImpl _soup;

    private JacobVPU _vpu;

    private Long _pid;

    private long _seq;

    protected void setUp() throws Exception {
        _completedOk = false;
        _terminate = false;
        _fault = null;
        _soup = new ExecutionQueueImpl(CoreBpelTest.class.getClassLoader());
        _vpu = new JacobVPU();
        _vpu.setContext(_soup);
        _vpu.registerExtension(BpelRuntimeContext.class, this);
        _pid = new Long(19355);
    }

    public Long getPid() {
        return _pid;
    }

    public boolean isVariableInitialized(VariableInstance variable) {
        return false;
    }

    public Long createScopeInstance(Long parentScopeId, OScope scopeType) {
        return new Long((long) (Math.random() * 1000L) + scopeType.getId());
    }

    public Node fetchVariableData(VariableInstance var, boolean forWriting) throws FaultException {
        return null;
    }

    public Node fetchVariableData(VariableInstance var, OMessageVarType.Part partname, boolean forWriting) throws FaultException {
        return null;
    }

    public String readProperty(VariableInstance var, OProcess.OProperty property) throws FaultException {
        return null;
    }

    public Node initializeVariable(VariableInstance var, Node initData) {
        return null;
    }

    public void commitChanges(VariableInstance var, Node changes) {
    }

    public boolean isCorrelationInitialized(CorrelationSetInstance cset) {
        return false;
    }

    public CorrelationKey readCorrelation(CorrelationSetInstance cset) {
        return null;
    }

    public void writeCorrelation(CorrelationSetInstance cset, CorrelationKey correlation) {
    }

    public void cancel(TimerResponse timerResponseChannel) {
    }

    public void completedOk() {
        _completedOk = true;
    }

    public void completedFault(FaultData faultData) {
        _fault = faultData;
    }

    public void select(PickResponse response, Date timeout, boolean createInstnace, Selector[] selectors) throws FaultException {
    }

    public void cancelOutstandingRequests(String channelId) {
    }

    public void reply(PartnerLinkInstance plink, String opName, String mexId, Element msg, QName fault) throws FaultException {
    }

    public String invoke(int aid, PartnerLinkInstance partnerLinkInstance, Operation operation, Element outboundMsg, InvokeResponse invokeResponseChannel) {
        return null;
    }

    public void registerTimer(TimerResponse timerChannel, Date timeToFire) {
    }

    public void terminate() {
        _terminate = true;
    }

    public void sendEvent(ProcessInstanceEvent event) {
    }

    public ExpressionLanguageRuntimeRegistry getExpLangRuntime() {
        return null;
    }

    public void initializePartnerLinks(Long parentScopeId, Collection<OPartnerLink> partnerLinks) {
    }

    public void testEmptyProcess() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        proc.getProcesScope().setActivity(new OEmpty(proc, proc.getProcesScope()));

        run(proc);

        assertTrue(_completedOk);
        assertFalse(_terminate);
        assertNull(_fault);
    }

    public void testThrow() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        OThrow othrow = new OThrow(proc, proc.getProcesScope());
        othrow.setFaultName(new QName("foo", "bar"));
        proc.getProcesScope().setActivity(othrow);

        run(proc);

        assertFalse(_completedOk);
        assertFalse(_terminate);
        assertEquals(_fault.getFaultName(), othrow.getFaultName());
    }

    public void testFaultHandling() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        OThrow othrow = new OThrow(proc, proc.getProcesScope());
        othrow.setFaultName(new QName("foo", "bar"));
        proc.getProcesScope().setActivity(othrow);
        proc.getProcesScope().setFaultHandler(new OFaultHandler(proc));
        OCatch ocatch = new OCatch(proc, proc.getProcesScope());
        proc.getProcesScope().getFaultHandler().getCatchBlocks().add(ocatch);
        ocatch.setActivity(new OEmpty(proc, ocatch));
        run(proc);

        assertTrue(_completedOk);
        assertFalse(_terminate);
        assertNull(_fault);
    }

    public void testOneElementSequence() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        OSequence sequence = new OSequence(proc, proc.getProcesScope());
        proc.getProcesScope().setActivity(sequence);
        sequence.getSequence().add(new OEmpty(proc, sequence));

        run(proc);

        assertTrue(_completedOk);
        assertFalse(_terminate);
        assertNull(_fault);
    }

    public void testTwoElementSequence() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        OSequence sequence = new OSequence(proc, proc.getProcesScope());
        proc.getProcesScope().setActivity(sequence);
        sequence.getSequence().add(new OEmpty(proc, sequence));
        sequence.getSequence().add(new OEmpty(proc, sequence));

        run(proc);

        assertTrue(_completedOk);
        assertFalse(_terminate);
        assertNull(_fault);
    }

    public void testEmptyFlow() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        proc.getProcesScope().setActivity(new OFlow(proc, proc.getProcesScope()));

        run(proc);

        assertTrue(_completedOk);
        assertFalse(_terminate);
        assertNull(_fault);
    }

    public void testSingleElementFlow() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        OFlow flow = new OFlow(proc, proc.getProcesScope());
        proc.getProcesScope().setActivity(flow);
        flow.getParallelActivities().add(new OEmpty(proc, flow));

        run(proc);

        assertTrue(_completedOk);
        assertFalse(_terminate);
        assertNull(_fault);
    }

    public void testTwoElementFlow() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        OFlow flow = new OFlow(proc, proc.getProcesScope());
        proc.getProcesScope().setActivity(flow);
        flow.getParallelActivities().add(new OEmpty(proc, flow));
        flow.getParallelActivities().add(new OEmpty(proc, flow));

        run(proc);

        assertTrue(_completedOk);
        assertFalse(_terminate);
        assertNull(_fault);
    }

    public void testFlowTermination() {
        OProcess proc = new OProcess("2.0");
        proc.setProcesScope(new OScope(proc, null));
        OFlow flow = new OFlow(proc, proc.getProcesScope());
        proc.getProcesScope().setActivity(flow);
        OThrow othrow = new OThrow(proc, flow);
        othrow.setFaultName(new QName("foo", "bar"));
        flow.getParallelActivities().add(othrow);
        flow.getParallelActivities().add(new OEmpty(proc, flow));
        flow.getParallelActivities().add(new OEmpty(proc, flow));
        flow.getParallelActivities().add(new OEmpty(proc, flow));
        flow.getParallelActivities().add(new OEmpty(proc, flow));
        flow.getParallelActivities().add(new OEmpty(proc, flow));
        flow.getParallelActivities().add(new OEmpty(proc, flow));

        run(proc);

        assertFalse(_completedOk);
        assertFalse(_terminate);
        assertEquals(_fault.getFaultName(), othrow.getFaultName());
    }

    private void run(OProcess proc) {
        _vpu.inject(new PROCESS(proc));
        for (int i = 0; i < 100000 && !_completedOk && _fault == null && !_terminate; ++i) {
            _vpu.execute();
        }

        assertTrue(_soup.isComplete());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            _soup.write(bos);
            _soup.read(new ByteArrayInputStream(bos.toByteArray()));
            // check empty soup.
        } catch (Exception ex) {

        }
    }

    public long genId() {
        return _seq++;
    }

    public Element getPartnerResponse(String mexId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Element getMyRequest(String mexId) {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getPartnerFault(String mexId) {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getPartnerResponseType(String mexId) {
        // TODO Auto-generated method stub
        return null;
    }

    public Node convertEndpointReference(Element epr, Node targetNode) {
        return null;
    }

    public Node getPartData(Element message, Part part) {
        return null;
    }

    public Element getSourceEPR(String mexId) {
        return null;
    }

    public void writeEndpointReference(PartnerLinkInstance variable, Element data) throws FaultException {
    }

    public Element fetchMyRoleEndpointReferenceData(PartnerLinkInstance pLink) {
        // TODO Auto-generated method stub
        return null;
    }

    public Element fetchPartnerRoleEndpointReferenceData(PartnerLinkInstance pLink) throws FaultException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isPartnerRoleEndpointInitialized(PartnerLinkInstance pLink) {
        // TODO Auto-generated method stub
        return false;
    }

    public String fetchMySessionId(PartnerLinkInstance pLink) {
        // TODO Auto-generated method stub
        return null;
    }

    public String fetchPartnersSessionId(PartnerLinkInstance pLink) {
        // TODO Auto-generated method stub
        return null;
    }

    public void initializePartnersSessionId(PartnerLinkInstance pLink, String session) {
        // TODO Auto-generated method stub

    }

    public String getSourceSessionId(String mexId) {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerActivityForRecovery(ActivityRecovery channel, long activityId, String reason, Date dateTime, Element data, String[] actions, int retries) {
    }

    public void unregisterActivityForRecovery(ActivityRecovery channel) {
    }

    public void recoverActivity(String channel, long activityId, String action, FaultData fault) {
    }

    public String getPartnerFaultExplanation(String mexid) {
        return null;
    }

    public void releasePartnerMex(String mexId, boolean instanceSucceeded) {
        // TODO Auto-generated method stub

    }

    public void initializeExternalVariable(VariableInstance instance, HashMap<String, String> keymap) {
        // TODO Auto-generated method stub

    }

    public Node readExtVar(Variable variable, Node reference) throws ExternalVariableModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node readVariable(Long scopeInstanceId, String varname, boolean forWriting) throws FaultException {
        // TODO Auto-generated method stub
        return null;
    }

    public ValueReferencePair writeExtVar(Variable variable, Node reference, Node value) throws ExternalVariableModuleException {
        // TODO Auto-generated method stub
        return null;
    }

    public Node writeVariable(VariableInstance var, Node changes) {
        // TODO Auto-generated method stub
        return null;
    }

    public URI getBaseResourceURI() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node getProcessProperty(QName propertyName) {
        // TODO Auto-generated method stub
        return null;
    }

    public QName getProcessQName() {
        // TODO Auto-generated method stub
        return null;
    }

    public Date getCurrentEventDateTime() {
        // TODO Auto-generated method stub
        return null;
    }

    public ClassLoader getProcessClassLoader() {
        return getClass().getClassLoader();
    }

    public void processOutstandingRequest(PartnerLinkInstance partnerLink,
        String opName, String mexId, String mexRef) {
        // TODO Auto-generated method stub
    }

    public PartnerRoleConfig getConfigForPartnerLink(OPartnerLink pLink) {
        return new PartnerRoleConfig(null, true);
    }

    public void forceFlush() {
        // TODO Auto-generated method stub
        
    }

    public void checkInvokeExternalPermission() {}

    public Node initializeVariable(VariableInstance var, ScopeFrame scopeFrame,
			Node val) throws ExternalVariableModuleException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node fetchVariableData(VariableInstance variable,
			ScopeFrame scopeFrame, boolean forWriting) throws FaultException {
		// TODO Auto-generated method stub
		return null;
	}

	public ExtensionOperation createExtensionActivityImplementation(QName name) {
		// TODO Auto-generated method stub
		return null;
	}
}
