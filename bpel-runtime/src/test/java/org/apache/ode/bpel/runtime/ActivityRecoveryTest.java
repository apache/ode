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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.engine.BpelManagementFacadeImpl;
import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.o.OFailureHandling;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.pmapi.ProcessInfoDocument;
import org.apache.ode.bpel.pmapi.TActivityInfo;
import org.apache.ode.bpel.pmapi.TActivityStatus;
import org.apache.ode.bpel.pmapi.TFailureInfo;
import org.apache.ode.bpel.pmapi.TFailuresInfo;
import org.apache.ode.bpel.pmapi.TInstanceInfo;
import org.apache.ode.bpel.pmapi.TInstanceInfoList;
import org.apache.ode.bpel.pmapi.TInstanceStatus;
import org.apache.ode.bpel.pmapi.TInstanceSummary;
import org.apache.ode.bpel.pmapi.TScopeInfo;
import org.apache.ode.bpel.pmapi.TScopeRef;
import org.apache.ode.utils.DOMUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.api.Action;
import org.jmock.lib.action.ActionSequence;
import org.jmock.lib.action.CustomAction;
import org.jmock.lib.action.ReturnValueAction;

/**
 * Test activity recovery and failure handling.
 */
public class ActivityRecoveryTest extends TestCase {
    Mockery context = new Mockery();

	// the maximum ammout of time to wait for an instance to reach a
	// desired status or for an activity to become available for recovery
    static final int MAX_WAIT = 10000;
    // poll interval
    static final int DELAY = 100;
	
    static final String   NAMESPACE = "http://ode.apache.org/bpel/unit-test";
    static final String[] ACTIONS = new String[]{ "retry", "cancel", "fault" };
    MockBpelServer        _server;
    BpelManagementFacade  _management;
    QName                 _processQName;
    QName                 _processId;
    private TestService _testService;

    static {
        // disable deferred process instance cleanup for faster testing
        System.setProperty(BpelServerImpl.DEFERRED_PROCESS_INSTANCE_CLEANUP_DISABLED_NAME, "true");
    }

    /**
     * The process calls the failing service, simulated by a call to invoke.
     * The method returns true if the call succeeded, false for failure.
     * If the process completes, it calls the completed method.
     */
    interface TestService {
        public boolean invoke(); 
        public void completed();
    }

    public void testInvokeSucceeds() throws Exception {
        // Since the service invocation succeeds, the process completes.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(1).of(_testService).invoke(); inSequence(seq); will(returnValue(true));
            exactly(1).of(_testService).completed(); inSequence(seq);
        }});

        execute("FailureToRecovery");
        assertNotNull(lastInstance(TInstanceStatus.COMPLETED));
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testFailureWithRecoveryAfterRetry() throws Exception {
        // Since the invocation is repeated 3 times, the process completes after
        // the third (successful) invocation.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(3).of(_testService).invoke(); inSequence(seq); will(failTheFirst(2));
            exactly(1).of(_testService).completed(); inSequence(seq);
        }});

        execute("FailureToRecovery");
        assertNotNull(lastInstance(TInstanceStatus.COMPLETED));
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testFailureWithManualRecovery() throws Exception {
        // Recovery required after three failures. Only one attempt made after recovery.
        // Only the fifth invocation succeeds.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(5).of(_testService).invoke(); inSequence(seq); will(failTheFirst(4));
            exactly(1).of(_testService).completed(); inSequence(seq);
        }});

        execute("FailureToRecovery");
        recover("retry");
        recover("retry");
        assertNotNull(lastInstance(TInstanceStatus.COMPLETED));
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testFailureWithFaultAction() throws Exception {
        // Recovery required after three failures. Only one attempt made after recovery.
        // Use the last failure to cause a fault.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(4).of(_testService).invoke(); inSequence(seq); will(failTheFirst(4));
            never(_testService).completed(); inSequence(seq);
        }});

        execute("FailureToRecovery");
        recover("retry");
        recover("fault");
        assertNotNull(lastInstance(TInstanceStatus.FAILED));
        assertTrue(OFailureHandling.FAILURE_FAULT_NAME.equals(lastInstance(null).getFaultInfo().getName()));
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testFailureWithCancelAction() throws Exception {
        // Recovery required after three failures. Only one attempt made after recovery.
        // Use the last failure to cancel the activity, allowing the process to complete.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(4).of(_testService).invoke(); inSequence(seq); will(failTheFirst(4));
            exactly(1).of(_testService).completed(); inSequence(seq);
        }});

        execute("FailureToCancel");
        recover("retry");
        recover("cancel");
        assertNotNull(lastInstance(TInstanceStatus.COMPLETED));
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testImmediateFailure() throws Exception {
        // This process does not attempt to retry, entering recovery immediately.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(1).of(_testService).invoke(); inSequence(seq); will(returnValue(false));
            never(_testService).completed(); inSequence(seq);
        }});

        execute("FailureNoRetry");
        assertRecovery(1, ACTIONS);
        context.assertIsSatisfied();
    }

    public void testImmediateFailureAndFault() throws Exception {
        // This process responds to failure with a fault.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(1).of(_testService).invoke(); inSequence(seq); will(returnValue(false));
            never(_testService).completed(); inSequence(seq);
        }});

        execute("FailureToFault");
        assertNotNull(lastInstance(TInstanceStatus.FAILED));
        assertEquals(OFailureHandling.FAILURE_FAULT_NAME, lastInstance(TInstanceStatus.FAILED).getFaultInfo().getName());
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testImmediateFailureAndFault2() throws Exception {
        // This process responds to failure with a fault.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(1).of(_testService).invoke(); inSequence(seq); will(returnValue(false));
            never(_testService).completed(); inSequence(seq);
        }});

        execute("FailureToFault2");
        assertNotNull(lastInstance(TInstanceStatus.FAILED));
        assertEquals(OFailureHandling.FAILURE_FAULT_NAME, lastInstance(TInstanceStatus.FAILED).getFaultInfo().getName());
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testFailureHandlingInheritence() throws Exception {
        // Since the invocation is repeated 3 times, the process completes after
        // the third (successful) invocation.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(3).of(_testService).invoke(); inSequence(seq); will(failTheFirst(2));
            exactly(1).of(_testService).completed(); inSequence(seq);
        }});

        execute("FailureInheritence");
        assertNotNull(lastInstance(TInstanceStatus.COMPLETED));
        assertNoFailures();
        context.assertIsSatisfied();
    }

    public void testInstanceSummary() throws Exception {
        _processQName = new QName(NAMESPACE, "FailureToRecovery");
        _processId = new QName(NAMESPACE, "FailureToRecovery-1");
        // Failing the first three times and recovering, the process completes.
        final Sequence seq = context.sequence("sequence");
        context.checking(new Expectations() {{
            exactly(4).of(_testService).invoke(); inSequence(seq); will(failTheFirst(3));
            exactly(1).of(_testService).completed(); inSequence(seq);
        }});

        _server.invoke(_processQName, "instantiate", DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:RequestElement"));
        _server.waitForBlocking();
        recover("retry"); // Completed.
        // Failing the first three times, we can then fault the process.
        context.checking(new Expectations() {{
            exactly(3).of(_testService).invoke(); will(failTheFirst(3));
        }});

        _server.invoke(_processQName, "instantiate", DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:RequestElement"));
        _server.waitForBlocking();
        recover("fault"); // Faulted.
        // Failing the first three times, we can then leave it waiting for recovery.
        context.checking(new Expectations() {{
            exactly(3).of(_testService).invoke(); will(failTheFirst(3));
        }});

        _server.invoke(_processQName, "instantiate", DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:RequestElement"));
        _server.waitForBlocking(); // Active, recovery.
        // Stay active, awaiting recovery.

        TInstanceSummary summary = _management.getProcessInfo(_processId).getProcessInfo().getInstanceSummary();
        for (TInstanceSummary.Instances instances : summary.getInstancesArray()) {
            switch (instances.getState().intValue()) {
              case TInstanceStatus.INT_COMPLETED:
                assertTrue(instances.getCount() == 1);
                break;
              case TInstanceStatus.INT_FAILED:
                assertTrue(instances.getCount() == 1);
                break;
              case TInstanceStatus.INT_ACTIVE:
                assertTrue(instances.getCount() == 1);
                break;
              default:
                assertTrue(instances.getCount() == 0);
                break;
            }
        }
        assertTrue(summary.getFailures().getCount() == 1);
        assertNotNull(summary.getFailures().getDtFailure());
        context.assertIsSatisfied();
    }


    protected void setUp() throws Exception {
        // Override testService in test case.
        _testService = context.mock(TestService.class);
        // We use one partner to simulate failing service and receive message upon process completion.
        final MessageExchangeContext partner = context.mock(MessageExchangeContext.class);
        // Some processes will complete, but not all.
        context.checking(new Expectations() {{
            atMost(1).of(partner).invokePartner(with(aMexWithOpnameIs("respond"))); will(new CustomAction("process completed") {
                public Object invoke(org.jmock.api.Invocation invocation) throws Throwable {
                    _testService.completed();
                    return null;
                }
            });
        }});

        context.checking(new Expectations() {{
            atLeast(1).of(partner).invokePartner(with(aMexWithOpnameIs("invoke"))); will(new CustomAction("invoke failing service") {
                public Object invoke(org.jmock.api.Invocation invocation) throws Throwable {
                    PartnerRoleMessageExchange mex = (PartnerRoleMessageExchange) invocation.getParameter(0);
                    if (_testService.invoke()) {
                        Message response = mex.createMessage(mex.getOperation().getOutput().getMessage().getQName());
                        response.setMessage(DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:ResponseElement"));
                        mex.reply(response);
                    } else {
                        mex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, "BangGoesInvoke", null);
                    }
                    return null;
                }
            });
        }});

        context.checking(new Expectations() {{
            atMost(1).of(partner).onAsyncReply(with(any(MyRoleMessageExchange.class))); will(new CustomAction("async reply") {
                public Object invoke(org.jmock.api.Invocation invocation) throws Throwable {
                    return null;
                }
            });
        }});
        
        _server = new MockBpelServer() {
            protected MessageExchangeContext createMessageExchangeContext() {
                return partner;
            }
        };
        _server.deploy(new File(new URI(this.getClass().getResource("/recovery").toString())));
        _management = new BpelManagementFacadeImpl(_server._server,_server._store);
    }

    protected void tearDown() throws Exception {
        _management.delete(null);
        _server.shutdown();
    }

    /**
     * Returns a stub that will fail (return false) for the first n number of times,
     * and on the last call succeed (return true).
     */
    protected Action failTheFirst(int times) {
        Action[] actions = new Action[times + 1];
        for (int i = 0; i < times; ++i)
            actions[i] = new ReturnValueAction(false);
        actions[times] = new ReturnValueAction(true);
        return new ActionSequence(actions);
    }

    public static class MexOpNameMatcher extends TypeSafeMatcher<PartnerRoleMessageExchange> {
        private String opName;

        public MexOpNameMatcher(String opName) {
            this.opName = opName;
        }

        public boolean matchesSafely(PartnerRoleMessageExchange mex) {
            return mex.getOperation().getName().equals(opName);
        }

        public void describeTo(Description description) {
            description.appendText("a mex invoking ").appendValue(opName);
        }
    }

    @Factory
    public static Matcher<PartnerRoleMessageExchange> aMexWithOpnameIs(String opName) {
        return new MexOpNameMatcher(opName);
    }

    /**
     * Call this to execute the process so it fails the specified number of times.
     * Returns when the process has either completed, or waiting for recovery to happen.
     */
    protected void execute(String process) throws Exception {
        _management.delete(null);
        // We need the process QName to make assertions on its state.
        _processQName = new QName(NAMESPACE, process);
        _processId = new QName(NAMESPACE, process + "-1");
        _server.invoke(_processQName, "instantiate", DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:RequestElement"));
    }

    protected void assertNoFailures() throws Exception {
        TFailuresInfo failures = lastInstance(null).getFailures();
        assertTrue(failures == null || failures.getCount() == 0);
        failures = _management.getProcessInfo(_processId).getProcessInfo().getInstanceSummary().getFailures();
        assertTrue(failures == null || failures.getCount() == 0);
    }

    /**
     * Blocks until the last instance reaches the desired status or throws an
     * exception if MAX_WAIT is exceeded.
     * 
     * @param expected
     * @throws Exception
     */
    protected TInstanceInfo lastInstance(TInstanceStatus.Enum expected) throws Exception {
    	int counter = 0;
    	do {
    		TInstanceInfo info = getInstanceInfo();
    		if (info != null  && (expected == null || info.getStatus() == expected)) {
    			return info;
    		}
    		if (counter * DELAY > MAX_WAIT) {
    			throw new Exception("Timed out wait for instance to reach "+expected+
    					" status. Actual status: "+(info==null?"missing instance" : info.getStatus()));
    		}
    		counter++;
    		Thread.sleep(DELAY);
    	} while (true);
    }
    /**
     * get the instance info for the last instance 
     */
    private TInstanceInfo getInstanceInfo() {
    	TInstanceInfoList instances = _management.listInstances("", "", 1000).getInstanceInfoList();
		int size = instances.sizeOfInstanceInfoArray();
		if (size > 0) {
			return instances.getInstanceInfoArray(instances.sizeOfInstanceInfoArray() - 1);
		}
		return null;
    }

    /**
     * Asserts that the process has one activity in the recovery state.
     * @throws Exception 
     */
    protected void assertRecovery(int invoked, String[] actions) throws Exception {
        // Process is still active, none of the completed states.
        assertNotNull(lastInstance(TInstanceStatus.ACTIVE));
        // Tests here will only generate one failure.
        TInstanceInfo instance = lastInstance(null);
        int count = 30;
        int sleep = 0;
        while (true) {
            count--;
            if (count <= 0) throw new AssertionError("No failures info, which are required");
            Thread.sleep(sleep);
            sleep = 1000;
            TFailuresInfo failures = instance.getFailures();
            if (!(failures != null && failures.getCount() == 1)) continue;
            ProcessInfoDocument m = _management.getProcessInfo(_processId);
            if (!m.getProcessInfo().isSetInstanceSummary()) continue;
            failures = m.getProcessInfo().getInstanceSummary().getFailures();
            if (!(failures != null && failures.getCount() == 1)) continue;
            break;
        }
        // Look for individual activities inside the process instance.
        ArrayList<TActivityInfo> recoveries = getRecoveriesInScope(instance, null, null);
        assertTrue(recoveries.size() == 1);
        TFailureInfo failure = recoveries.get(0).getFailure();
        assertTrue(failure.getRetries() == invoked - 1);
        assertTrue(failure.getReason().equals("BangGoesInvoke"));
        assertTrue(failure.getDtFailure() != null);
        java.util.HashSet<String> actionSet = new java.util.HashSet<String>();
        for (String action : failure.getActions().split(" "))
            actionSet.add(action);
        for (String action : actions)
            assertTrue(actionSet.remove(action));
    }

    /**
     * Performs the specified recovery action. Also asserts that there is one
     * recovery channel for the activity in question.
     */
    protected void recover(String action) throws Exception {
    	ArrayList<TActivityInfo> recoveries = getRecoveries();
        assertTrue(recoveries.size() == 1);
        TActivityInfo activity = recoveries.get(0);
        assertNotNull(activity);
        _management.recoverActivity(Long.valueOf(getInstanceInfo().getIid()), Long.valueOf(activity.getAiid()), action);
        _server.waitForBlocking();
    }
    /**
     * Blocks for until there are recoveries available or throws an exception
     * if MAX_WAIT exceeded
     */
    private ArrayList<TActivityInfo> getRecoveries() throws Exception {
    	TInstanceInfo instance = null;
    	int counter = 0;
    	do {
    		instance = getInstanceInfo();
    		if (instance != null) {
    			ArrayList<TActivityInfo> recoveries = getRecoveriesInScope(instance, null, null);
    			if (recoveries.size() > 0) {
    				return recoveries;
    			}
    		}
    		if (counter * DELAY > MAX_WAIT) {
    			throw new Exception("Timed out wait for recovery activities");
    		}
    		Thread.sleep(DELAY);
    		counter++;
    	} while (true);
    }

    protected ArrayList<TActivityInfo> getRecoveriesInScope(TInstanceInfo instance, TScopeInfo scope,
                                                            ArrayList<TActivityInfo> recoveries) throws Exception {
        if (scope == null)
            scope = _management.getScopeInfoWithActivity(instance.getRootScope().getSiid(), true).getScopeInfo();
        if (recoveries == null)
            recoveries = new ArrayList<TActivityInfo>();
        TScopeInfo.Activities activities = scope.getActivities();
        for (int i = 0; i < activities.sizeOfActivityInfoArray(); ++i) {
            TActivityInfo activity = activities.getActivityInfoArray(i);
            if (activity.getStatus() == TActivityStatus.FAILURE) {
                assertNotNull(activity.getFailure());
                recoveries.add(activity);
            } else
                assertNull(activity.getFailure());
        }
        for (TScopeRef ref : scope.getChildren().getChildRefArray()) {
            TScopeInfo child = _management.getScopeInfoWithActivity(ref.getSiid(), true).getScopeInfo();
            if (child != null)
                getRecoveriesInScope(instance, child, recoveries);
        }
        return recoveries;
    }

}
