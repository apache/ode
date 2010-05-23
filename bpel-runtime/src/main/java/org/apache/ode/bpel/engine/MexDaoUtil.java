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

import javax.xml.namespace.QName;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.FailureType;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.w3c.dom.Element;

/**
 * Some handy utilities methods for dealing with MEX impls.
 *  
 */
class MexDaoUtil {

    static void setFailed(MessageExchangeImpl mex, FailureType ftype, String explanation) {
        mex.setStatus(Status.FAILURE);
        mex.setFaultExplanation(explanation);
        mex.setFailure(ftype, explanation, null);
    }

    static void setFaulted(MessageExchangeImpl mex, QName faultType, Element faultmsg) {
        mex.setStatus(Status.FAULT);
        Message flt = mex.createMessage(faultType);
        flt.setMessage(faultmsg);
        mex.setFault(faultType, flt);
    }

    static void setResponse(MessageExchangeImpl mex, QName responseType, Element response) {
        mex.setStatus(Status.RESPONSE);
        mex.setFault(null, null);
        Message resp = mex.createMessage(responseType);
        resp.setMessage(response);
        mex.setResponse(resp);
    }

	public static void setFailure(PartnerRoleMessageExchangeImpl mex, FailureType type, String description, Element details) {
        mex.replyWithFailure(type, description, details);
	}
}
