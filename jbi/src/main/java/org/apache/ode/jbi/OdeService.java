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
package org.apache.ode.jbi;

import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange.MessageExchangePattern;
import org.apache.ode.bpel.iapi.MessageExchange.Status;
import org.apache.ode.jbi.msgmap.Mapper;
import org.apache.ode.jbi.msgmap.MessageTranslationException;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.messaging.*;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * Bridge JBI (consumer) to ODE (provider).
 */
public class OdeService implements JbiMessageExchangeProcessor {

  private static final Log __log = LogFactory.getLog(OdeService.class);

  /** utility for tracking outstanding JBI message exchanges. */
  private final JbiMexTracker _jbiMexTracker = new JbiMexTracker();
  
  /** JBI-Generated Endpoint */
  private ServiceEndpoint _internal;

  /** External endpoint. */
  private ServiceEndpoint _external;

  private OdeContext  _ode;

  private QName _odeServiceId;

  private QName _jbiServiceName;

  private String _jbiPortName;

  private Element _serviceref;
  
  
  public OdeService(OdeContext odeContext, QName odeServiceId, QName jbiServiceName, String jbiPortName)
      throws Exception {
    _ode = odeContext;
    _odeServiceId = odeServiceId;
    _jbiServiceName = jbiServiceName;
    _jbiPortName = jbiPortName;
  }

  /**
   * Do the JBI endpoint activation.
   *
   * @throws JBIException
   */
  public void activate() throws JBIException {
    if (_serviceref ==  null) {
      ServiceEndpoint[] candidates = _ode.getContext().getExternalEndpointsForService(_jbiServiceName);
      if (candidates.length != 0) {
        _external = candidates[0];
      }
    }
    _internal = _ode.getContext().activateEndpoint(_jbiServiceName, _jbiPortName);
    if (__log.isDebugEnabled()) {
      __log.debug("Activated service " + _jbiServiceName + " on port " +  _jbiPortName);
    }
    // TODO: Is there a race situation here?
  }

  /**
   * Deactivate endpoints in JBI.
   */
  public void deactivate() throws JBIException {
    _ode.getContext().deactivateEndpoint(_internal);
    __log.debug("Dectivated service " + _jbiServiceName + " with port " +  _jbiPortName);
  }

  public ServiceEndpoint getInternalServiceEndpoint() {
    return _internal;
  }
  
  public ServiceEndpoint getExternalServiceEndpoint() {
    return _external;
  }
  

  public void onJbiMessageExchange(javax.jbi.messaging.MessageExchange jbiMex) throws MessagingException {
    if (jbiMex.getRole() != javax.jbi.messaging.MessageExchange.Role.PROVIDER) {
      String errmsg ="Message exchange is not in PROVIDER role as expected: " + jbiMex.getExchangeId(); 
      __log.fatal(errmsg);
      throw new IllegalArgumentException(errmsg);
    }

    if (jbiMex.getStatus() != ExchangeStatus.ACTIVE) {
      // We can forget about the exchange.
      _jbiMexTracker.consume(jbiMex.getExchangeId());
      return;
    }
    
    if (jbiMex.getPattern().equals(org.apache.ode.jbi.MessageExchangePattern.IN_ONLY)) {
      boolean success = false;
      Exception err = null;
      try {
        invokeOde(jbiMex, ((InOnly)jbiMex).getInMessage());
        success = true;
      } catch (Exception ex) {
        __log.error("Error invoking ODE.",ex);
        err = ex;
      } finally {
        if (!success) {
          jbiMex.setStatus(ExchangeStatus.ERROR);
          jbiMex.setError(err);
        } else {
          jbiMex.setStatus(ExchangeStatus.DONE);
        }        
      }
    } else if (jbiMex.getPattern().equals(org.apache.ode.jbi.MessageExchangePattern.IN_OUT)) {
      boolean success = false;
      Exception err = null;
      try {
        invokeOde(jbiMex, ((InOut)jbiMex).getInMessage());
        success = true;
      } catch (Exception ex) {
        __log.error("Error invoking ODE.", ex);
        err = ex;
      } finally {
        if (!success) {
          jbiMex.setError(err);
          jbiMex.setStatus(ExchangeStatus.ERROR);
        }
      }
    } else {
      __log.error("JBI MessageExchange " + jbiMex.getExchangeId() + " is of an unsupported pattern "
          + jbiMex.getPattern());
      jbiMex.setStatus(ExchangeStatus.ERROR);
      jbiMex.setError(new Exception("Unknown message exchange pattern: " + jbiMex.getPattern()));
    }

  }

  /**
   * Called from {@link MessageExchangeContextImpl#onAsyncReply(MyRoleMessageExchange)}
   * @param mex message exchenge
   */
  public void onResponse(MyRoleMessageExchange mex) {
    javax.jbi.messaging.MessageExchange jbiMex = _jbiMexTracker.consume(mex.getClientId());
    if (jbiMex == null) {
      __log.warn("Ingorning unknown async reply: " + mex);
      return;
    }

    switch (mex.getStatus()) {
      case FAULT:
        outResponseFault(mex, jbiMex);
        break;
      case RESPONSE:
        outResponse(mex, jbiMex);
        break;
      case FAILURE:
        outFailure(mex, jbiMex);
        break;
      default :
        __log.warn("Received ODE message exchange in unexpected state: " + mex.getStatus());
    }
  }

  /**
   * Forward a JBI input message to ODE.
   *
   * @param jbiMex
   */
  private void invokeOde(javax.jbi.messaging.MessageExchange jbiMex, 
      NormalizedMessage request) throws Exception {

    // If this has already been tracked, we will not invoke!
    if (_jbiMexTracker.track(jbiMex)) {
      __log.debug("Skipping JBI MEX " + jbiMex.getExchangeId() + ", already received!");
      return;
    }
    
    QName serviceName = jbiMex.getEndpoint().getServiceName();

    _ode.getTransactionManager().begin();

    boolean success = false;
    MyRoleMessageExchange odeMex = null;
    try {
      if (__log.isDebugEnabled()) {
      __log.debug("invokeOde() JBI exchangeId=" + jbiMex.getExchangeId() + " serviceName=" + serviceName + " operation=" + jbiMex.getOperation().getLocalPart() );
      __log.debug("invokeOde() ODE servideId=" + _odeServiceId + " serviceName=" + _jbiServiceName + " port=" + _jbiPortName + " internal=" + _internal );
      }
      odeMex = _ode._server.getEngine().createMessageExchange(
        jbiMex.getExchangeId(),
        _odeServiceId,
        null,
        jbiMex.getOperation().getLocalPart());

      if (odeMex.getOperation() != null) {
        javax.wsdl.Message msgdef = odeMex.getOperation().getInput().getMessage();
        Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
        Mapper mapper = _ode.findMapper(request,odeMex.getOperation());
        if (mapper == null) {
          String errmsg = "Could not find a mapper for request message for JBI MEX " + jbiMex.getExchangeId()
            + "; ODE MEX " + odeMex.getMessageExchangeId() + " is failed. "; 
          __log.error(errmsg);
          throw new MessageTranslationException(errmsg);
          
        }
        odeMex.setProperty(Mapper.class.getName(), mapper.getClass().getName());
        mapper.toODE(odeRequest, request, msgdef);
        odeMex.invoke(odeRequest);
        
        // Handle the response if it is immediately available.
        if (odeMex.getStatus() != Status.ASYNC) {
          __log.debug("ODE MEX "  + odeMex  + " completed SYNCHRONOUSLY.");
          onResponse(odeMex);
          _jbiMexTracker.consume(jbiMex.getExchangeId());
        } else {
          __log.debug("ODE MEX " + odeMex + " completed ASYNCHRONOUSLY.");
        }
      } else {
        __log.error("ODE MEX "  +odeMex + " was unroutable.");
        
      }

      success = true;
      // For one-way invocation we do not need to maintain the association
      if (odeMex.getMessageExchangePattern() != MessageExchangePattern.REQUEST_RESPONSE)
        _jbiMexTracker.consume(jbiMex.getExchangeId());

    } finally {
      if (success) {
        __log.debug("Commiting ODE MEX "  + odeMex );
        _ode.getTransactionManager().commit();
      } else {
        __log.debug("Rolling back ODE MEX "  + odeMex );
        _jbiMexTracker.consume(jbiMex.getExchangeId());
        _ode.getTransactionManager().rollback();
        
      }
    }
    
  }

  private void outFailure(MyRoleMessageExchange odeMex, javax.jbi.messaging.MessageExchange jbiMex) {
    try {
      jbiMex.setError(new Exception("MEXFailure"));
      jbiMex.setStatus(ExchangeStatus.ERROR);
      // TODO: get failure codes out of the message.
    } catch (MessagingException ex) {
      __log.fatal("Error bridging ODE out response: ", ex);
    }
  }

  private void outResponse(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) {
    InOut inout = (InOut)jbiMex;
    
    try {
      NormalizedMessage nmsg = inout.createMessage();
      String mapperName = mex.getProperty(Mapper.class.getName());
      Mapper mapper = _ode.getMapper(mapperName);
      if (mapper == null) {
        String errmsg = "Message-mapper " + mapperName + " used in ODE MEX " 
          + mex.getMessageExchangeId() + " is no longer available.";
        __log.error(errmsg);
        throw new MessageTranslationException(errmsg);
      }
      
      mapper.toNMS(nmsg,mex.getResponse(),
            mex.getOperation().getOutput().getMessage());
      
      inout.setOutMessage(nmsg);
      _ode.getChannel().send(inout);
      
    } catch (MessagingException ex) {
      __log.error("Error bridging ODE out response: ", ex);
      inout.setError(ex);
    } catch (MessageTranslationException e) {
      __log.error("Error translating ODE message " + mex.getResponse()
          + " to NMS format!", e);
      inout.setError(e);
    }
  }

  private void outResponseFault(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) {

    InOut inout = (InOut)jbiMex;

    try {
      Fault flt = inout.createFault();
      String mapperName = mex.getProperty(Mapper.class.getName());
      Mapper mapper = _ode.getMapper(mapperName);
      if (mapper == null) {
        String errmsg = "Message-mapper " + mapperName + " used in ODE MEX " 
          + mex.getMessageExchangeId() + " is no longer available.";
        __log.error(errmsg);
        throw new MessageTranslationException(errmsg);
      }
      
      mapper.toNMS(flt,mex.getResponse(),
            mex.getOperation().getOutput().getMessage());
      inout.setFault(flt);
      _ode.getChannel().send(inout);
    } catch (MessagingException e) {
      __log.error("Error bridging ODE fault response: ", e);
      inout.setError(e);
    } catch (MessageTranslationException mte) {
      __log.error("Error translating ODE fault message " + mex.getFaultResponse()
          + " to NMS format!", mte);
      inout.setError(mte);
    }
  }

  public QName getJbiServiceName() {
    return _jbiServiceName;
  }

  
  public QName getOdeServiceId() {
    return _jbiServiceName;
  }

  
  /**
   * Class for tracking outstanding message exchanges from JBI.
   */
  private static class JbiMexTracker {
    /** Outstanding JBI-initiated exchanges: mapping for JBI MEX ID to JBI MEX */
    private Map<String, javax.jbi.messaging.MessageExchange> _outstandingJbiExchanges =
      new HashMap<String, javax.jbi.messaging.MessageExchange>();

    synchronized boolean track(javax.jbi.messaging.MessageExchange jbiMex) {
      boolean found = _outstandingJbiExchanges.containsKey(jbiMex.getExchangeId());
      _outstandingJbiExchanges.put(jbiMex.getExchangeId(), jbiMex);
      return found;
    }

    synchronized javax.jbi.messaging.MessageExchange consume(String clientId) {
      return _outstandingJbiExchanges.remove(clientId);
    }
    
    
  }
}
