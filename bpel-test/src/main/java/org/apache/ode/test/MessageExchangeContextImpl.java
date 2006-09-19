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

import javax.wsdl.PortType;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.w3c.dom.Element;

/**
 * This is a simple MessageExchangeContext implementation
 * that only supports a set of "well known" portTypes used
 * for testing.
 *
 *
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

	// Probe Service is a simple concatination service
	private QName probePT = new QName("http://ode/bpel/unit-test/ProbeService.wsdl","probeMessagePT");
	
	public void invokePartner(PartnerRoleMessageExchange mex)
			throws ContextException {
		QName calledPT = mex.getPortType().getQName();
		
		if (calledPT.equals(probePT)) {
			invokeProbeService(mex);
		}
		

	}

	public void onAsyncReply(MyRoleMessageExchange myRoleMex)
			throws BpelEngineException {

	}
	
	private void invokeProbeService(PartnerRoleMessageExchange prmx) {
		Message msg = prmx.getRequest();
		Element elm1 = prmx.getRequest().getPart("probeName");
		Element elm2 = prmx.getRequest().getPart("probeData");
		
		if ( elm1 != null && elm2 != null ) {
			String cat = elm2.getTextContent()+" -> "+elm1.getTextContent();
			elm2.setTextContent(cat);
			msg.setMessagePart("probeData", elm2);
            final Message response = prmx.createMessage(prmx.getOperation().getOutput().getMessage().getQName());

            response.setMessage(msg.getMessage());
			prmx.reply(response);
		}
		
		

	}

}
