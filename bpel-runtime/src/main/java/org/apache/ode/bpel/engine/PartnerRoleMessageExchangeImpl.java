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

package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.replayer.Replayer;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.JobType;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.xml.namespace.QName;

public class PartnerRoleMessageExchangeImpl extends MessageExchangeImpl implements PartnerRoleMessageExchange {
    private static final Log LOG = LogFactory.getLog(PartnerRoleMessageExchangeImpl.class);

    private PartnerRoleChannel _channel;
    private EndpointReference _myRoleEPR;
    private int responsesReceived;
    
    protected PartnerRoleMessageExchangeImpl(BpelEngineImpl engine, MessageExchangeDAO dao, PortType portType,
            Operation operation, 
            EndpointReference epr,
            EndpointReference myRoleEPR,
            PartnerRoleChannel channel) {
        super(engine, dao);
        _myRoleEPR = myRoleEPR;
        setPortOp(portType, operation);
        _channel = channel;
    }

    public void replyOneWayOk() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("replyOneWayOk mex=" + getMessageExchangeId());
        }
        setStatus(Status.ASYNC);
    }

    public void replyAsync() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("replyAsync mex=" + getMessageExchangeId());
        }
        setStatus(Status.ASYNC);
    }

    public void replyWithFault(QName faultType, Message outputFaultMessage) throws BpelEngineException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("replyWithFault mex=" + getMessageExchangeId());
        }
        boolean isAsync = isAsync();
        setFault(faultType, outputFaultMessage);
        if (isAsync)
            continueAsync();
    }

    public void reply(Message response) throws BpelEngineException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("reply mex=" + getMessageExchangeId());
        }
        boolean isAsync = isAsync();
        setResponse(response);
        if (isAsync)
            continueAsync();

    }
    
    @Override
    void setResponse(Message outputMessage) throws BpelEngineException {
    	// If pub-sub is enabled, we may receive multiple responses. In such cases,
    	// don't bail out if our status is already set to RESPONSE.
		if (++responsesReceived > 1) {
			if (getSubscriberCount() > 1 && getStatus() == Status.RESPONSE) {
	    		return;
	    	}
		}
    	super.setResponse(outputMessage);
    }

    public void replyWithFailure(FailureType type, String description, Element details) throws BpelEngineException {
        if (LOG.isDebugEnabled()) {
            String msg = new StringBuilder("replyWithFailure mex=").append(getMessageExchangeId())
                    .append(" failureType=").append(type)
                    .append(" description=").append(description)
                    .append(" details=").append(details==null?null:DOMUtils.domToString(details))
                    .toString();
            LOG.debug(msg);
        }
        boolean isAsync = isAsync();
        setFailure(type, description, details);
        if (isAsync)
            continueAsync();
    }

    /**
     * Continue from the ASYNC state.
     * 
     */
    private void continueAsync() {
        // If there is no channel waiting for us, there is nothing to do.
        if (getDAO().getChannel() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("no channel on mex=" + getMessageExchangeId());
            }
            return;
        }
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("create work event for mex=" + getMessageExchangeId());
        }
        JobDetails we = new JobDetails();
        we.setInstanceId(getDAO().getInstance().getInstanceId());
        we.setType(JobType.INVOKE_RESPONSE);
        we.setInMem(_engine._activeProcesses.get(getDAO().getProcess().getProcessId()).isInMemory());
        we.setChannel(getDAO().getChannel());
        we.setMexId(getDAO().getMessageExchangeId());
        Replayer replayer = Replayer.replayer.get();
        if (replayer == null) {
            if (we.getInMem())
                _engine._contexts.scheduler.scheduleVolatileJob(true, we);
            else
                _engine._contexts.scheduler.schedulePersistedJob(we, null);
        } else {
            replayer.scheduler.schedulePersistedJob(we, null);
        }
    }

    /**
     * Check if we are in the ASYNC state.
     * 
     * @return
     */
    private boolean isAsync() {
        return getStatus() == Status.ASYNC;
    }

    public QName getCaller() {
        return _dao.getProcess().getProcessId();
    }

    public String toString() {
        try {
            return "{PartnerRoleMex#" + getMessageExchangeId() + " [PID " + getCaller() + "] calling " + _epr + "."
                    + getOperationName() + "(...) Status " + getStatus() + "}";

        } catch (Throwable t) {
            return "{PartnerRoleMex#????}";
        }

    }

    public PartnerRoleChannel getChannel() {
        return _channel;
    }

    public EndpointReference getMyRoleEndpointReference() {
        return _myRoleEPR;
    }

}
