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
package org.apache.ode.bpel.rtrep.v2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Collection;
import java.util.Map;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.ScopeEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStartedEvent;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.rapi.*;
import org.apache.ode.bpel.rtrep.v2.channels.*;
import org.apache.ode.bpel.extension.ExtensionOperation;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.iapi.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.junit.Assert;

/**
 * Test core BPEL processing capabilities.
 */
public class CoreBpelTest extends TestCase implements OdeInternalInstance {
    private boolean _completedOk;

    private boolean _terminate;

    private FaultInfo _fault;

    private ExecutionQueueImpl _soup;

    private JacobVPU _vpu;

    private Long _pid;

    private long _seq;

    protected void setUp() throws Exception {
        _completedOk = false;
        _terminate = false;
        _fault = null;
        _soup = new ExecutionQueueImpl(CoreBpelTest.class.getClassLoader());
        _vpu = new JacobVPU(_soup);
        _vpu.registerExtension(OdeRTInstanceContext.class, this);
        _pid = (long) 19355;
    }

    public Long getInstanceId() {
        return _pid;
    }

    public Long createScopeInstance(Long parentScopeId, String scopename, int scopemodelid) {
        return (long) (Math.random() * 1000L) + scopemodelid;
    }

    public void completedOk() {
        _completedOk = true;
    }

    public void completedFault(FaultData faultData) {
        _fault = faultData;
    }

    public void terminate() {
        _terminate = true;
    }

    public boolean isCorrelationInitialized(CorrelationSetInstance correlationSet) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String readProperty(VariableInstance variable, OProcess.OProperty property) throws FaultException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void writeCorrelation(CorrelationSetInstance cset, CorrelationKey ckeyVal) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node initializeVariable(VariableInstance var, ScopeFrame scopeFrame, Node val) throws ExternalVariableModuleException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Long createScopeInstance(Long scopeInstanceId, OScope scopedef) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> getProperties(String mexId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void initializeResource(Long parentScopeId, OResource resource, String url) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void checkResourceRoute(ResourceInstance resourceInstance, String mexRef, PickResponseChannel pickResponseChannel, int selectorIdx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void initializeInstantiatingUrl(String url) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getInstantiatingUrl() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void associateEvent(PartnerLinkInstance plinkInstance, String opName, CorrelationKey key, String mexRef, String mexDAO) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void associateEvent(ResourceInstance resourceInstance, String mexRef, String scopeIid) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void reply(ResourceInstance resource, String bpelmex, Element element, QName fault) throws FaultException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void initializePartnerLinks(Long parentScopeId, Collection<OPartnerLink> partnerLinks) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancelOutstandingRequests(String channelId) {
    }

    public String invoke(String invokeId, PartnerLinkInstance instance, Operation operation, Element outboundMsg, Object object) throws FaultException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String invoke(String requestId, Resource resource, Element outgoingMessage) throws FaultException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void registerTimer(TimerResponseChannel timerChannel, Date future) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void select(PickResponseChannel pickResponseChannel, Date timeout, boolean createInstance, Selector[] selectors) throws FaultException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void checkResourceRoute(String url, String method, String mexRef, PickResponseChannel pickResponseChannel, int selectorIdx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public CorrelationKey readCorrelation(CorrelationSetInstance cset) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ExpressionLanguageRuntimeRegistry getExpLangRuntime() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void cancel(PickResponseChannel responseChannel) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void commitChanges(VariableInstance var, ScopeFrame scopeFrame, Node value) throws ExternalVariableModuleException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node fetchVariableData(VariableInstance var, ScopeFrame scopeFrame, OMessageVarType.Part part, boolean forWriting) throws FaultException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sendEvent(ScopeEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void sendEvent(ProcessInstanceStartedEvent evt) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isVariableInitialized(VariableInstance var) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Node fetchVariableData(VariableInstance variable, ScopeFrame scopeFrame, boolean forWriting) throws FaultException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void reply(PartnerLinkInstance plink, String opName, String bpelmex, Element element, QName fault) throws FaultException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void testEmptyProcess() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        proc.processScope.activity = new OEmpty(proc, proc.processScope);

        run(proc);

        Assert.assertTrue(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertNull(_fault);
    }

    public void testThrow() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        OThrow othrow = new OThrow(proc, proc.processScope);
        othrow.faultName = new QName("foo", "bar");
        proc.processScope.activity = othrow;

        run(proc);

        Assert.assertFalse(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertEquals(_fault.getFaultName(), othrow.faultName);
    }

    public void testFaultHandling() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        OThrow othrow = new OThrow(proc, proc.processScope);
        othrow.faultName = new QName("foo", "bar");
        proc.processScope.activity = othrow;
        proc.processScope.faultHandler = new OFaultHandler(proc);
        OCatch ocatch = new OCatch(proc, proc.processScope);
        proc.processScope.faultHandler.catchBlocks.add(ocatch);
        ocatch.activity = new OEmpty(proc, ocatch);
        run(proc);

        Assert.assertTrue(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertNull(_fault);
    }

    public void testOneElementSequence() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        OSequence sequence = new OSequence(proc, proc.processScope);
        proc.processScope.activity = sequence;
        sequence.sequence.add(new OEmpty(proc, sequence));

        run(proc);

        Assert.assertTrue(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertNull(_fault);
    }

    public void testTwoElementSequence() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        OSequence sequence = new OSequence(proc, proc.processScope);
        proc.processScope.activity = sequence;
        sequence.sequence.add(new OEmpty(proc, sequence));
        sequence.sequence.add(new OEmpty(proc, sequence));

        run(proc);

        Assert.assertTrue(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertNull(_fault);
    }

    public void testEmptyFlow() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        proc.processScope.activity = new OFlow(proc, proc.processScope);

        run(proc);

        Assert.assertTrue(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertNull(_fault);
    }

    public void testSingleElementFlow() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        OFlow flow = new OFlow(proc, proc.processScope);
        proc.processScope.activity = flow;
        flow.parallelActivities.add(new OEmpty(proc, flow));

        run(proc);

        Assert.assertTrue(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertNull(_fault);
    }

    public void testTwoElementFlow() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        OFlow flow = new OFlow(proc, proc.processScope);
        proc.processScope.activity = flow;
        flow.parallelActivities.add(new OEmpty(proc, flow));
        flow.parallelActivities.add(new OEmpty(proc, flow));

        run(proc);

        Assert.assertTrue(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertNull(_fault);
    }

    public void testFlowTermination() {
        OProcess proc = new OProcess("2.0");
        proc.processScope = new OScope(proc, null);
        OFlow flow = new OFlow(proc, proc.processScope);
        proc.processScope.activity = flow;
        OThrow othrow = new OThrow(proc, flow);
        othrow.faultName = new QName("foo", "bar");
        flow.parallelActivities.add(othrow);
        flow.parallelActivities.add(new OEmpty(proc, flow));
        flow.parallelActivities.add(new OEmpty(proc, flow));
        flow.parallelActivities.add(new OEmpty(proc, flow));
        flow.parallelActivities.add(new OEmpty(proc, flow));
        flow.parallelActivities.add(new OEmpty(proc, flow));
        flow.parallelActivities.add(new OEmpty(proc, flow));

        run(proc);

        Assert.assertFalse(_completedOk);
        Assert.assertFalse(_terminate);
        Assert.assertEquals(_fault.getFaultName(), othrow.faultName);
    }

    private void run(OProcess proc) {
        _vpu.inject(new PROCESS(proc));
        for (int i = 0; i < 100000 && !_completedOk && _fault == null && !_terminate; ++i) {
            _vpu.execute();
        }

        Assert.assertTrue(_soup.isComplete());

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

    public void setInstantiatingMex(String mexId) {
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

    public Node getPartData(Element message, OMessageVarType.Part part) {
        return null;
    }

    public Element getSourceEPR(String mexId) {
        return null;
    }

    public void writeEndpointReference(PartnerLinkInstance variable, Element data) {
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

    public void registerActivityForRecovery(ActivityRecoveryChannel channel, long activityId, String reason,
                                            Date dateTime, Element data, String[] actions, int retries) {
    }

    public void unregisterActivityForRecovery(ActivityRecoveryChannel channel) {
    }

    public void recoverActivity(String channel, long activityId, String action, FaultData fault) {
    }

    public String getPartnerFaultExplanation(String mexid) {
      return null;
    }

    public void releasePartnerMex(String mexId, boolean instanceSucceeded) {
        // TODO Auto-generated method stub
        
    }

    public void forceFlush() {
        // TODO Auto-generated method stub
        
    }

    public void forceRollback() {
        // TODO Auto-generated method stub
        
    }
    
	public ExtensionOperation createExtensionActivityImplementation(QName name) {
		// TODO Auto-generated method stub
		return null;
	}

    public void initializeExternalVariable(VariableInstance instance, HashMap<String, String> keymap) {
        // TODO Auto-generated method stub
        
    }

	public Node readExtVar(OScope.Variable variable, Node reference) throws ExternalVariableModuleException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node readVariable(Long scopeInstanceId, String varname, boolean forWriting) throws FaultException {
		// TODO Auto-generated method stub
		return null;
	}

	public VariableContext.ValueReferencePair writeExtVar(OScope.Variable variable, Node reference, Node value)
            throws ExternalVariableModuleException {
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

	public int getRetryDelay() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isFirstTry() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRetryable() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setRetriedOnce() {
		// TODO Auto-generated method stub
		
	}

	public void setRetriesDone() {
		// TODO Auto-generated method stub
		
	}

	public void setAtomicScope(boolean atomicScope) {
		// TODO Auto-generated method stub
		
	}

	public Node getProcessProperty(QName propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void completeExtensionActivity(String channelId, FaultData faultData) {
		// TODO Auto-generated method stub
	}
}
