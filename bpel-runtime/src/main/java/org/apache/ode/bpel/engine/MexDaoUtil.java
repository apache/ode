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
