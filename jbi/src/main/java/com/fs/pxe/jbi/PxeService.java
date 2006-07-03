/*
 * File:      $Id: JbiToPxeBridge.java 492 2006-01-02 16:12:09Z holger $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.jbi;

import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange.MessageExchangePattern;
import com.fs.pxe.bpel.iapi.MessageExchange.Status;
import com.fs.pxe.jbi.msgmap.Mapper;
import com.fs.pxe.jbi.msgmap.MessageTranslationException;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.JBIException;
import javax.jbi.messaging.*;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

/**
 * Bridge JBI (consumer) to PXE (provider).
 */
public class PxeService implements JbiMessageExchangeProcessor {

  private static final Log __log = LogFactory.getLog(PxeService.class);

  /** utility for tracking outstanding JBI message exchanges. */
  private final JbiMexTracker _jbiMexTracker = new JbiMexTracker();
  
  /** JBI-Generated Endpoint */
  private ServiceEndpoint _internal;

  /** External endpoint. */
  private ServiceEndpoint _external;

  private PxeContext  _pxe;

  private QName _serviceName;

  private String _portName;

  private Element _serviceref;
  
  
  public PxeService(PxeContext pxeContext, QName serviceName, String portName)
      throws Exception {
    _pxe = pxeContext;
    _serviceName = serviceName;
    _portName = portName;
  }

  /**
   * Do the JBI endpoint activation.
   *
   * @throws JBIException
   */
  public void activate() throws JBIException {
    if (_serviceref ==  null) {
      ServiceEndpoint[] candidates = _pxe.getContext().getExternalEndpointsForService(_serviceName);
      if (candidates.length != 0) {
        _external = candidates[0];
      }
    }
    
    _internal = _pxe.getContext().activateEndpoint(_serviceName, _portName);
    // TODO: Is there a race situation here?
    
    __log.debug("Activated service " + _serviceName + " with port " +  _portName);
  }

  /**
   * Deactivate endpoints in JBI.
   */
  public void deactivate() throws JBIException {
    _pxe.getContext().deactivateEndpoint(_internal);
    __log.debug("Dectivated service " + _serviceName + " with port " +  _portName);
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
    
    if (jbiMex.getPattern().equals(com.fs.pxe.jbi.MessageExchangePattern.IN_ONLY)) {
      boolean success = false;
      Exception err = null;
      try {
        invokePxe(jbiMex, ((InOnly)jbiMex).getInMessage());
        success = true;
      } catch (Exception ex) {
        __log.error("Error invoking PXE.",ex);
        err = ex;
      } finally {
        if (!success) {
          jbiMex.setStatus(ExchangeStatus.ERROR);
          jbiMex.setError(err);
        } else {
          jbiMex.setStatus(ExchangeStatus.DONE);
        }        
      }
    } else if (jbiMex.getPattern().equals(com.fs.pxe.jbi.MessageExchangePattern.IN_OUT)) {
      boolean success = false;
      Exception err = null;
      try {
        invokePxe(jbiMex, ((InOut)jbiMex).getInMessage());
        success = true;
      } catch (Exception ex) {
        __log.error("Error invoking PXE.", ex);
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
        __log.warn("Received PXE message exchange in unexpected state: " + mex.getStatus());
    }
  }

  /**
   * Forward a JBI input message to PXE.
   *
   * @param jbiMex
   */
  private void invokePxe(javax.jbi.messaging.MessageExchange jbiMex, 
      NormalizedMessage request) throws Exception {

    // If this has already been tracked, we will not invoke!
    if (_jbiMexTracker.track(jbiMex)) {
      __log.debug("Skipping JBI MEX " + jbiMex.getExchangeId() + ", already received!");
      return;
    }
    
    QName serviceName = jbiMex.getEndpoint().getServiceName();

    _pxe.getTransactionManager().begin();

    boolean success = false;
    MyRoleMessageExchange pxeMex = null;
    try {
       pxeMex = _pxe._server.getEngine().createMessageExchange(
          jbiMex.getExchangeId(),
          serviceName,
          null,
          jbiMex.getOperation().getLocalPart());

      if (pxeMex.getOperation() != null) {
        javax.wsdl.Message msgdef = pxeMex.getOperation().getInput().getMessage();
        Message pxeRequest = pxeMex.createMessage(pxeMex.getOperation().getInput().getMessage().getQName());
        Mapper mapper = _pxe.findMapper(request,pxeMex.getOperation());
        if (mapper == null) {
          String errmsg = "Could not find a mapper for request message for JBI MEX " + jbiMex.getExchangeId()
            + "; PXE MEX " + pxeMex.getMessageExchangeId() + " is failed. "; 
          __log.error(errmsg);
          throw new MessageTranslationException(errmsg);
          
        }
        pxeMex.setProperty(Mapper.class.getName(), mapper.getClass().getName());
        mapper.toPXE(pxeRequest, request, msgdef);
        pxeMex.invoke(pxeRequest);
        
        // Handle the response if it is immediately available.
        if (pxeMex.getStatus() != Status.ASYNC) {
          __log.debug("PXE MEX "  + pxeMex  + " completed SYNCHRONOUSLY.");
          onResponse(pxeMex);
          _jbiMexTracker.consume(jbiMex.getExchangeId());
        } else {
          __log.debug("PXE MEX " + pxeMex + " completed ASYNCHRONOUSLY.");
        }
      } else {
        __log.error("PXE MEX "  +pxeMex + " was unroutable.");
        
      }

      success = true;
      // For one-way invocation we do not need to maintain the association
      if (pxeMex.getMessageExchangePattern() != MessageExchangePattern.REQUEST_RESPONSE)
        _jbiMexTracker.consume(jbiMex.getExchangeId());

    } finally {
      if (success) {
        __log.debug("Commiting PXE MEX "  + pxeMex );
        _pxe.getTransactionManager().commit();
      } else {
        __log.debug("Rolling back PXE MEX "  + pxeMex );
        _jbiMexTracker.consume(jbiMex.getExchangeId());
        _pxe.getTransactionManager().rollback();
        
      }
    }
    
  }

  private void outFailure(MyRoleMessageExchange pxeMex, javax.jbi.messaging.MessageExchange jbiMex) {
    try {
      jbiMex.setError(new Exception("MEXFailure"));
      jbiMex.setStatus(ExchangeStatus.ERROR);
      // TODO: get failure codes out of the message.
    } catch (MessagingException ex) {
      __log.fatal("Error bridging PXE out response: ", ex);
    }
  }

  private void outResponse(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) {
    InOut inout = (InOut)jbiMex;
    
    try {
      NormalizedMessage nmsg = inout.createMessage();
      String mapperName = mex.getProperty(Mapper.class.getName());
      Mapper mapper = _pxe.getMapper(mapperName);
      if (mapper == null) {
        String errmsg = "Message-mapper " + mapperName + " used in PXE MEX " 
          + mex.getMessageExchangeId() + " is no longer available.";
        __log.error(errmsg);
        throw new MessageTranslationException(errmsg);
      }
      
      mapper.toNMS(nmsg,mex.getResponse(),
            mex.getOperation().getOutput().getMessage());
      
      inout.setOutMessage(nmsg);
      _pxe.getChannel().send(inout);
      
    } catch (MessagingException ex) {
      __log.error("Error bridging PXE out response: ", ex);
      inout.setError(ex);
    } catch (MessageTranslationException e) {
      __log.error("Error translating PXE message " + mex.getResponse()
          + " to NMS format!", e);
      inout.setError(e);
    }
  }

  private void outResponseFault(MyRoleMessageExchange mex, javax.jbi.messaging.MessageExchange jbiMex) {

    InOut inout = (InOut)jbiMex;

    try {
      Fault flt = inout.createFault();
      String mapperName = mex.getProperty(Mapper.class.getName());
      Mapper mapper = _pxe.getMapper(mapperName);
      if (mapper == null) {
        String errmsg = "Message-mapper " + mapperName + " used in PXE MEX " 
          + mex.getMessageExchangeId() + " is no longer available.";
        __log.error(errmsg);
        throw new MessageTranslationException(errmsg);
      }
      
      mapper.toNMS(flt,mex.getResponse(),
            mex.getOperation().getOutput().getMessage());
      inout.setFault(flt);
      _pxe.getChannel().send(inout);
    } catch (MessagingException e) {
      __log.error("Error bridging PXE fault response: ", e);
      inout.setError(e);
    } catch (MessageTranslationException mte) {
      __log.error("Error translating PXE fault message " + mex.getFaultResponse()
          + " to NMS format!", mte);
      inout.setError(mte);
    }
  }

  public QName getServiceName() {
    return _serviceName;
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
