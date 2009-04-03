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

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This is a simple MessageExchangeContext implementation
 * that only supports a set of "well known" portTypes used
 * for testing.
 *
 *
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {
	
	private static final String PROBE_NS = "http://ode/bpel/unit-test/ProbeService.wsdl";
	private static final String FAULT_NS = "http://ode/bpel/unit-test/FaultService.wsdl";

	// Probe Service is a simple concatination service
	private static final QName probePT = new QName(PROBE_NS,"probeMessagePT");
	private static final QName faultPT = new QName(FAULT_NS,"faultMessagePT");
	
	private Message currentResponse;
	
	public void invokePartner(PartnerRoleMessageExchange mex)
			throws ContextException {
		QName calledPT = mex.getPortType().getQName();
		
		if (calledPT.equals(probePT)) {
			invokeProbeService(mex);
		}
		
		if (calledPT.equals(faultPT)) {
			invokeFaultService(mex);
		}

	}

	public void onAsyncReply(MyRoleMessageExchange myRoleMex)
			throws BpelEngineException {
		Status mStat = myRoleMex.getStatus();
        if ( mStat == Status.RESPONSE ) {
			currentResponse = myRoleMex.getResponse();
		}
		myRoleMex.complete();
	}
	
	private void invokeProbeService(PartnerRoleMessageExchange prmx) {
		Message msg = prmx.getRequest();
		Element elm1 = prmx.getRequest().getPart("probeName");
		Element elm2 = prmx.getRequest().getPart("probeData");
		
		if ( elm1 != null && elm2 != null ) {
			String cat = elm2.getTextContent()+" -> "+elm1.getTextContent();
			elm2.setTextContent(cat);
			msg.setPart("probeData", elm2);
            final Message response = prmx.createMessage(prmx.getOperation().getOutput().getMessage().getQName());

            response.setMessage(msg.getMessage());
			prmx.reply(response);
		}
	}
	
	private void invokeFaultService(PartnerRoleMessageExchange prmx) {
		QName errorMsgType = new QName(FAULT_NS,"errorMessage");
		QName responseMsgType = new QName(FAULT_NS,"faultMessage");
		Message faultMsg = prmx.createMessage(errorMsgType);
		Message responseMsg = prmx.createMessage(responseMsgType);

		String ind1 = prmx.getRequest().getPart("faultIndicator1").getTextContent();
		String ind2 = prmx.getRequest().getPart("faultIndicator2").getTextContent();
		String inputData = prmx.getRequest().getPart("faultData").getTextContent();
		
		StringBuffer faultData = new StringBuffer("<message><errorID>FA-1</errorID><errorText>");
		faultData.append(inputData);
		faultData.append("</errorText></message>");
		
		StringBuffer responseData = new StringBuffer("<message><faultName>FA-NoFault</faultName><faultData>");
		responseData.append(inputData);
		responseData.append("</faultData></message>");
		
		
		Element faultResponse = null;
		Element response = null;
		try {
			faultResponse = DOMUtils.stringToDOM(faultData.toString());
			response = DOMUtils.stringToDOM(responseData.toString());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		// TODO: Question - how does one set parts that are of a simple xsd type?
		faultMsg.setMessage(faultResponse);
		responseMsg.setMessage(response);
		
		if ( ind1.equals("yes")){
			prmx.replyWithFault(new QName(FAULT_NS,"FaultMessage1"), faultMsg);
		} else {
			if ( ind2.equals("yes")){
				prmx.replyWithFault(new QName(FAULT_NS,"FaultMessage2"), faultMsg);
			} else {
				prmx.replyWithFault(new QName(FAULT_NS,"UnKnownFault"), faultMsg);
			}
		}

	}
	
	public Message getCurrentResponse() {
		return currentResponse;
	}
	
	public void clearCurrentResponse() {
		currentResponse = null;
	}

}
