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
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.pmapi.TActivityInfo;
import org.apache.ode.bpel.pmapi.TActivityStatus;
import org.apache.ode.bpel.pmapi.TFaultInfo;
import org.apache.ode.bpel.pmapi.TFailureInfo;
import org.apache.ode.bpel.pmapi.TFailuresInfo;
import org.apache.ode.bpel.pmapi.TInstanceInfo;
import org.apache.ode.bpel.pmapi.TInstanceStatus;
import org.apache.ode.bpel.pmapi.TScopeInfo;
import org.apache.ode.bpel.pmapi.TScopeRef;
import org.apache.ode.bpel.o.OFailureHandling;
import org.apache.ode.utils.DOMUtils;

/**
 * Test activity recovery and failure handling.
 */
public class ActivityRecoveryTest extends TestCase {

    static final String   NAMESPACE = "http://ode.apache.org/bpel/unit-test";
    static final String[] ACTIONS = new String[]{ "retry", "cancel", "fault" };
    int                   _invoked;
    int                   _failFor;
    boolean               _responseSent;
    MockBpelServer        _server;
    BpelManagementFacade  _management;
    QName                 _processQName;

    public void testSuccessfulInvoke() throws Exception { 
        execute("FailureToRecovery", 0);
        assertCompleted(true, 1, null);
    }

    public void testInvokeAndRetry() throws Exception {
        execute("FailureToRecovery", 2);
        assertCompleted(true, 3, null);
    }

    public void testRetryRecoveryAction() throws Exception {
        execute("FailureToRecovery", 4);
        assertRecovery(3, ACTIONS);
        recover("retry");
        assertRecovery(4, ACTIONS);
        recover("retry");
        assertCompleted(true, 5, null);
    }

    public void testFaultRecoveryAction() throws Exception {
        execute("FailureToRecovery", 4);
        assertRecovery(3, ACTIONS);
        recover("retry");
        assertRecovery(4, ACTIONS);
        recover("fault");
        assertCompleted(false, 4, OFailureHandling.FAILURE_FAULT_NAME);
    }

    public void testCancelRecoveryAction() throws Exception {
        execute("FailureToCancel", 4);
        assertRecovery(3, ACTIONS);
        recover("retry");
        assertRecovery(4, ACTIONS);
        recover("cancel");
        assertCompleted(true, 4, null);
    }

    public void testImmediateFailure() throws Exception {
        execute("FailureNoRetry", 1);
        assertRecovery(1, ACTIONS);
    }

    public void testImmediateFault() throws Exception {
        execute("FailureToFault", 2);
        assertCompleted(false, 1, OFailureHandling.FAILURE_FAULT_NAME);
    }

    public void testInheritence() throws Exception {
        execute("FailureInheritence", 2);
        assertCompleted(true, 3, null);
    }

    protected void setUp() throws Exception {
        _server = new MockBpelServer() {
            protected MessageExchangeContext createMessageExchangeContext() {
                return new MessageExchangeContext() {

                    public void invokePartner(final PartnerRoleMessageExchange mex) throws ContextException {
                        if (mex.getOperation().getName().equals("invoke")) {
                            // First fail, then succeed, that's the nature of a test case.
                            ++_invoked;
                            if (_invoked > _failFor) {
                                Message response = mex.createMessage(mex.getOperation().getOutput().getMessage().getQName());
                                response.setMessage(DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:ResponseElement"));
                                mex.reply(response);
                            } else {
                                mex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, "BangGoesInvoke", null);
                            }
                        } else if (mex.getOperation().getName().equals("respond")) {
                            // Happens when the process completes its last activity.
                            _responseSent = true; 
                        }
                    }

                    public void onAsyncReply(MyRoleMessageExchange myRoleMex) { }
                };
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
     * Call this to execute the process so it fails the specified number of times.
     * Returns when the process has either completed, or waiting for recovery to happen.
     */
    protected void execute(String process, int failFor) throws Exception {
        _failFor = failFor;
        _management.delete(null);
        _processQName = new QName(NAMESPACE, process);
        _server.invoke(_processQName, "instantiate",
                       DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:RequestElement"));
        _server.waitForBlocking();
    }

    /**
     * Asserts that the process has completed, successfully or not. If not,
     * it is either terminated, or faulted with the specified fault name.
     * This method also checks how many time the process invoked the service.
     */
    protected void assertCompleted(boolean successful, int invoked, QName faultName) {
        assertTrue(_invoked == invoked);
        TInstanceInfo instance = _management.listAllInstances().getInstanceInfoList().getInstanceInfoArray(0);
        // Process has completed, so no activities in the failure state.
        TFailuresInfo failures = instance.getFailures();
        assertTrue(failures == null || failures.getCount() == 0);
        failures = _management.getProcessInfo(_processQName).getProcessInfo().getInstanceSummary().getFailures();
        assertTrue(failures == null || failures.getCount() == 0);
        if (successful) {
            assertTrue(instance.getStatus() == TInstanceStatus.COMPLETED);
            assertTrue(_responseSent);
        } else if (faultName == null) {
            assertTrue(instance.getStatus() == TInstanceStatus.TERMINATED);
            assertFalse(_responseSent);
        } else {
            assertFalse(_responseSent);
            assertTrue(instance.getStatus() == TInstanceStatus.FAILED);
            TFaultInfo faultInfo = instance.getFaultInfo();
            assertTrue(faultInfo != null && faultInfo.getName().equals(faultName));
        }
    }

    /**
     * Asserts that the process has one activity in the recovery state.
     */
    protected void assertRecovery(int invoked, String[] actions) {
        // Process is still active, none of the completed states.
        TInstanceInfo instance = _management.listAllInstances().getInstanceInfoList().getInstanceInfoArray(0);
        assertTrue(instance.getStatus() == TInstanceStatus.ACTIVE);
        assertFalse(_responseSent);
        // Tests here will only generate one failure.
        TFailuresInfo failures = instance.getFailures();
        assertTrue(failures != null && failures.getCount() == 1);
        failures = _management.getProcessInfo(_processQName).getProcessInfo().getInstanceSummary().getFailures();
        assertTrue(failures != null && failures.getCount() == 1);
        // Look for individual activities inside the process instance.
        @SuppressWarnings("unused")
        TScopeInfo rootScope = _management.getScopeInfoWithActivity(instance.getRootScope().getSiid(), true).getScopeInfo();
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
    protected void recover(String action) {
        TInstanceInfo instance = _management.listAllInstances().getInstanceInfoList().getInstanceInfoArray(0);
        ArrayList<TActivityInfo> recoveries = getRecoveriesInScope(instance, null, null);
        assertTrue(recoveries.size() == 1);
        TActivityInfo activity = recoveries.get(0);
        assertNotNull(activity);
        _management.recoverActivity(Long.valueOf(instance.getIid()), Long.valueOf(activity.getAiid()), action);
        _server.waitForBlocking();
    }

    protected ArrayList<TActivityInfo> getRecoveriesInScope(TInstanceInfo instance, TScopeInfo scope,
                                                            ArrayList<TActivityInfo> recoveries) {
        if (instance == null)
            instance = _management.listAllInstances().getInstanceInfoList().getInstanceInfoArray(0);
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
        for (TScopeRef ref : scope.getChildren().getChildRefList()) {
            TScopeInfo child = _management.getScopeInfoWithActivity(ref.getSiid(), true).getScopeInfo();
            if (child != null)
                getRecoveriesInScope(instance, child, recoveries);
        }
        return recoveries;
    }

}
