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

import org.apache.ode.bpel.o.*;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.runtime.channels.InvokeResponseChannel;
import org.apache.ode.bpel.engine.*;
import org.apache.ode.bpel.iapi.*;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.pmapi.TInstanceInfo;
import org.apache.ode.bpel.pmapi.TInstanceStatus;
import org.apache.ode.bpel.pmapi.TFaultInfo;
import org.apache.ode.bpel.pmapi.TScopeInfo;
import org.apache.ode.bpel.pmapi.TScopeRef;
import org.apache.ode.bpel.pmapi.TActivityInfo;
import org.apache.ode.bpel.pmapi.TActivityStatus;
import org.apache.ode.utils.DOMUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import java.net.URI;
import java.io.File;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;
import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Test core BPEL processing capabilities.
 */
public class ActivityRecoveryTest extends TestCase {

  static final String   NAMESPACE = "http://ode.apache.org/bpel/unit-test";
  int                   _invoked;
  int                   _failFor;
  MockBpelServer        _server;
  BpelManagementFacade  _management;


  public void testSuccessfulInvoke() throws Exception { 
    execute(null, 0);
    assertCompleted(true, 1, null);
  }

  public void testInvokeAndRetry() throws Exception {
    execute(null, 2);
    assertCompleted(true, 3, null);
  }

  public void testRetryRecoveryAction() throws Exception {
    execute(null, 4);
    assertRecovery();
    recover("retry");
    assertRecovery();
    recover("retry");
    assertCompleted(true, 5, null);
  }

  public void testCancelRecoveryAction() throws Exception {
    execute(null, 4);
    assertRecovery();
    recover("retry");
    assertRecovery();
    recover("cancel");
    assertCompleted(true, 4, null);
  }

  public void testFaultRecoveryAction() throws Exception {
    execute(null, 4);
    assertRecovery();
    recover("retry");
    assertRecovery();
    recover("fault");
    assertCompleted(false, 4, FailureHandling.FAILURE_FAULT_NAME);
  }

  protected void setUp() throws Exception {
    _server = new MockBpelServer() {
      protected MessageExchangeContext createMessageExchangeContext() {
        return new MessageExchangeContext() {

          public void invokePartner(final PartnerRoleMessageExchange mex) throws ContextException {
            ++_invoked;
            if (_invoked > _failFor) {
              Message response = mex.createMessage(mex.getOperation().getOutput().getMessage().getQName());
              response.setMessage(DOMUtils.newDocument().createElementNS(NAMESPACE, "tns:ResponseElement"));
              mex.reply(response);
            } else
              mex.replyWithFailure(MessageExchange.FailureType.COMMUNICATION_ERROR, "Bang", null);
              //mex.replyWithFailure(FailureType, String, Element);
          }

          public void onAsyncReply(MyRoleMessageExchange myRoleMex) { }
        };
      }
    };
    _server.deploy(new File(new URI(this.getClass().getResource("/recovery").toString())));
    _management = _server.getBpelManagementFacade();
  }

  protected void tearDown() throws Exception {
    _server.getBpelManagementFacade().delete(null);
    _server.shutdown();
  }

  /**
   * Call this to execute the process so it fails the specified number of times.
   * Returns when the process has either completed, or waiting for recovery to happen.
   */
  protected void execute(String process, int failFor) throws Exception {
    _failFor = failFor;
    _server.getBpelManagementFacade().delete(null);
    _server.invoke(new QName(NAMESPACE, "InstantiatingService"), "instantiate",
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
    TInstanceInfo.Failures failures = instance.getFailures();
    assertTrue(failures == null || failures.getCount() == 0);
    if (successful) {
      assertTrue(instance.getStatus() == TInstanceStatus.COMPLETED);
    } else if (faultName == null) {
      assertTrue(instance.getStatus() == TInstanceStatus.TERMINATED);
    } else {
      assertTrue(instance.getStatus() == TInstanceStatus.FAILED);
      TFaultInfo faultInfo = instance.getFaultInfo();
      assertTrue(faultInfo != null && faultInfo.getName().equals(faultName));
    }
  }

  /**
   * Asserts that the process has one activity in the recovery state.
   */
  protected void assertRecovery() {
    // Test in aggregate to see how many activities we have in this state.
    TInstanceInfo instance = _management.listAllInstances().getInstanceInfoList().getInstanceInfoArray(0);
    TInstanceInfo.Failures failures = instance.getFailures();
    assertTrue(failures != null && failures.getCount() == 1);
    // Look for individual activities inside the process instance.
    TScopeInfo rootScope = _management.getScopeInfoWithActivity(instance.getRootScope().getSiid(), true).getScopeInfo();
    assertTrue(getRecoveriesInScope(instance, null, null).size() == 1);
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
      TScopeInfo child = _server.getBpelManagementFacade().getScopeInfoWithActivity(ref.getSiid(), true).getScopeInfo();
      if (child != null)
        getRecoveriesInScope(instance, child, recoveries);
    }
    return recoveries;
  }

}
