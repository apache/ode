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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.engine.MessageExchangeInterceptor.InterceptorContext;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.EndpointReference;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;

import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.Status;


class MyRoleMessageExchangeImpl extends MessageExchangeImpl 
  implements MyRoleMessageExchange {
  
  private static final Log __log = LogFactory.getLog(MyRoleMessageExchangeImpl.class);
  
  public MyRoleMessageExchangeImpl(
      BpelEngineImpl engine, 
      MessageExchangeDAO mexdao) {
    super(engine, mexdao);
  }


  public CorrelationStatus getCorrelationStatus() {
    return CorrelationStatus.valueOf(getDAO().getCorrelationStatus());
  }

  
  void setCorrelationStatus(CorrelationStatus status) {
    getDAO().setCorrelationStatus(status.toString());
  }

  /**
   * Process the message-exchange interceptors.
   * 
   * @param mex
   *            message exchange
   * @return <code>true</code> if execution should continue,
   *         <code>false</code> otherwise
   */
  private boolean processInterceptors(MyRoleMessageExchangeImpl mex) {
  	InterceptorContextImpl ictx = new InterceptorContextImpl(_engine._contexts.dao.getConnection(), null);
  	
      for (MessageExchangeInterceptor i : _engine.getGlobalInterceptors())
      	if (!processInterceptor(i, mex, ictx))
      		return false;
      	
      
      return true;
  }

  private boolean processInterceptor(MessageExchangeInterceptor i, MyRoleMessageExchangeImpl mex, InterceptorContext ictx) {
      boolean cont = i.onBpelServerInvoked(mex, ictx);
      if (!cont) {
          __log.debug("interceptor " + i + " caused invoke on " + this + "to be aborted");
          if (mex.getStatus() == Status.REQUEST) {
              __log.debug("aborting interceptor " + i + " did not set message exchange status, assuming failure");
              mex.setFailure(MessageExchange.FailureType.ABORTED, __msgs.msgInterceptorAborted(mex
                      .getMessageExchangeId(), i.toString()), null);
          }
          return false;
      }

      return true;
  }

  public void invoke(Message request) {
    if (request == null) {
      String errmsg = "Must pass non-null message to invoke()!";
      __log.fatal(errmsg);
      throw new NullPointerException(errmsg);
    }
    
    _dao.setRequest(((MessageImpl)request)._dao);
    _dao.setStatus(MessageExchange.Status.REQUEST.toString());

    if (!processInterceptors(this))
    	return;
    
    BpelProcess target = _engine.route(getDAO().getCallee(), request);

    if (__log.isDebugEnabled())
      __log.debug("invoke() EPR= " + _epr + " ==> " + target);


    if (target == null) {
      if (__log.isWarnEnabled()) 
        __log.warn(__msgs.msgUnknownEPR("" + _epr));
      
      setCorrelationStatus(MyRoleMessageExchange.CorrelationStatus.UKNOWN_ENDPOINT);
      setFailure(MessageExchange.FailureType.UNKNOWN_ENDPOINT, null,null);
    } else {
      target.invokeProcess(this);
    }
    
  }

  public void complete() {
  }

  public QName getServiceName() {
    return getDAO().getCallee();
  }

  public void setClientId(String clientKey) {
    getDAO().setCorrelationId(clientKey);
  }


  public String getClientId() {
    return getDAO().getCorrelationId();
  }
  
  public String toString() {
    try {
      return "{MyRoleMex#" + getMessageExchangeId()
      + " [Client " + getClientId() + "] calling " + getServiceName() + "." + getOperationName() + "(...)}";
    } catch (Throwable t) {
      return "{MyRoleMex#???}";
    }
  }

}
