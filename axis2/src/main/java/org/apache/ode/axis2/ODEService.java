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

package org.apache.ode.axis2;

import org.apache.ode.bpel.engine.BpelServerImpl;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.epr.WSAEndpoint;
import org.apache.ode.bom.wsdl.Definition4BPEL;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.GUID;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.transaction.TransactionManager;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A running service, encapsulates the Axis service, its receivers and
 * our receivers as well.
 */
public class ODEService {

  private static final Log __log = LogFactory.getLog(ODEService.class);
  private static final int TIMEOUT = 2 * 60 * 1000;

  private AxisService _axisService;
  private BpelServerImpl _server;
  private TransactionManager _txManager;
  private Definition _wsdlDef;
  private QName _serviceName;
  private String _portName;
  private Map<String,ResponseCallback> _waitingCallbacks;

  public ODEService(AxisService axisService, Definition4BPEL def, QName serviceName, String portName,
                    BpelServerImpl server, TransactionManager txManager) {
    _axisService = axisService;
    _server = server;
    _txManager = txManager;
    _wsdlDef = def;
    _serviceName = serviceName;
    _portName = portName;
    _waitingCallbacks = Collections.synchronizedMap(new HashMap<String, ResponseCallback>());
  }

  public void onAxisMessageExchange(MessageContext msgContext, MessageContext outMsgContext,
                                    SOAPFactory soapFactory) throws AxisFault {
    boolean success = true;
    MyRoleMessageExchange odeMex = null;
    ResponseCallback callback = null;
    try {
      _txManager.begin();

      // Creating mesage exchange
      String messageId = new GUID().toString();
      odeMex = _server.getEngine().createMessageExchange(""+messageId, _serviceName, null,
              msgContext.getAxisOperation().getName().getLocalPart());
      __log.debug("ODE routed to operation " + odeMex.getOperation() + " from service " + _serviceName);

      if (odeMex.getOperation() != null) {
        // Preparing message to send to ODE
        Message odeRequest = odeMex.createMessage(odeMex.getOperation().getInput().getMessage().getQName());
        Element msgContent = SOAPUtils.unwrap(OMUtils.toDOM(msgContext.getEnvelope().getBody().getFirstElement()),
                _wsdlDef, odeMex.getOperation().getInput().getMessage(), _serviceName);
        readHeader(msgContext, odeMex);
        odeRequest.setMessage(msgContent);

        // Preparing a callback just in case we would need one.
        if (odeMex.getOperation().getOutput() != null) {
          callback = new ResponseCallback();
          _waitingCallbacks.put(odeMex.getClientId(), callback);
        }

        if (__log.isDebugEnabled()) {
          __log.debug("Invoking ODE using MEX " + odeMex);
          __log.debug("Message content:  " + DOMUtils.domToString(odeRequest.getMessage()));
        }
        // Invoking ODE
        odeMex.invoke(odeRequest);
      } else {
        success = false;
      }
    } catch(Exception e) {
      e.printStackTrace();
      success = false;
      throw new AxisFault("An exception occured when invoking ODE.", e);
    } finally {
      if (success) {
        __log.debug("Commiting ODE MEX "  + odeMex );
        try {
          _txManager.commit();
        } catch (Exception e) {
          __log.error("COMMIT FAILED!", e);
          success = false;
        }
      }
      if (!success) {
        __log.error("Rolling back ODE MEX "  + odeMex );
        try {
          _txManager.rollback();
        } catch (Exception e) {
          throw new AxisFault("ROLLBACK FAILED!", e);
        }
      }

      if (odeMex.getOperation() != null) {
        boolean timeout = false;
        // Invocation response could be delayed, if so we have to wait for it.
        if (odeMex.getStatus() == MessageExchange.Status.ASYNC) {
          odeMex = callback.getResponse(TIMEOUT);
          if (odeMex == null) timeout = true;
        } else {
          // Callback wasn't necessary, cleaning up
          _waitingCallbacks.remove(odeMex.getMessageExchangeId());
        }

        if (outMsgContext != null) {
          SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
          outMsgContext.setEnvelope(envelope);

          // Hopefully we have a response
          __log.debug("Handling response for MEX " + odeMex);
          if (timeout) {
            __log.error("Timeout when waiting for response to MEX " + odeMex);
            success = false;
          } else {
            try {
              _txManager.begin();
              onResponse(odeMex, outMsgContext);
            } catch (Exception e) {
              try {
                _txManager.rollback();
              } catch (Exception ex) {
                throw new AxisFault("Rollback failed!", ex);
              }
              throw new AxisFault("An exception occured when invoking ODE.", e);
            } finally {
              try {
                _txManager.commit();
              } catch (Exception e) {
                throw new AxisFault("Commit failed!", e);
              }
            }
          }
        }
      }
    }
    if (!success) throw new AxisFault("Message was either unroutable or timed out!");
  }

  public void notifyResponse(MyRoleMessageExchange mex) {
    ResponseCallback callback = _waitingCallbacks.get(mex.getClientId());
    if (callback == null) {
      __log.error("No active service for message exchange: "  + mex);
    } else {
      callback.onResponse(mex);
      _waitingCallbacks.remove(mex.getClientId());
    }
  }

  public boolean respondsTo(QName serviceName, QName portTypeName) {
    boolean result = _serviceName.equals(serviceName);
    result = result && _wsdlDef.getService(_serviceName).getPort(_portName)
            .getBinding().getPortType().getQName().equals(portTypeName);
    return result;
  }

  private void onResponse(MyRoleMessageExchange mex, MessageContext msgContext) throws AxisFault {
    switch (mex.getStatus()) {
      case FAULT:
        throw new AxisFault(null, mex.getFault(), null, null, OMUtils.toOM(mex.getFaultResponse().getMessage()));
      case ASYNC:
      case RESPONSE:
        Element response = SOAPUtils.wrap(mex.getResponse().getMessage(), _wsdlDef, _serviceName,
                mex.getOperation(), mex.getOperation().getOutput().getMessage());
        msgContext.getEnvelope().getBody().addChild(OMUtils.toOM(response));
        writeHeader(msgContext, mex);
        break;
      case FAILURE:
        // TODO: get failure codes out of the message.
        throw new AxisFault("Message exchange failure!");
      default :
        __log.warn("Received ODE message exchange in unexpected state: " + mex.getStatus());
    }
  }

  /**
   * Extracts endpoint information from Axis MessageContext (taken from WSA headers) to
   * stuff them into ODE mesage exchange.
   */
  private void readHeader(MessageContext msgContext, MyRoleMessageExchange odeMex) {
    Object otse = msgContext.getProperty("targetSessionEndpoint");
    Object ocse = msgContext.getProperty("callbackSessionEndpoint");
    if (otse != null) {
      Element serviceEpr = (Element) otse;
      WSAEndpoint endpoint = new WSAEndpoint();
      endpoint.set(serviceEpr);
      odeMex.setEndpointReference(endpoint);
    }
    if (ocse != null) {
      Element serviceEpr = (Element) ocse;
      WSAEndpoint endpoint = new WSAEndpoint();
      endpoint.set(serviceEpr);
      odeMex.setCallbackEndpointReference(endpoint);
    }
  }

  /**
   * Extracts endpoint information from ODE message exchange to stuff them into
   * Axis MessageContext.
   */
  private void writeHeader(MessageContext msgContext, MyRoleMessageExchange odeMex) {
    if (odeMex.getEndpointReference() != null) {
      msgContext.setProperty("targetSessionEndpoint", odeMex.getEndpointReference());
      msgContext.setProperty("soapAction",
              SOAPUtils.getSoapAction(_wsdlDef, _serviceName, _portName, odeMex.getOperationName()));
    }
    if (odeMex.getCallbackEndpointReference() != null) {
      msgContext.setProperty("callbackSessionEndpoint", odeMex.getCallbackEndpointReference());
    }
  }

  public AxisService getAxisService() {
    return _axisService;
  }

  class ResponseCallback {
    private MyRoleMessageExchange _mmex;
    private boolean _timedout;

    synchronized boolean onResponse(MyRoleMessageExchange mmex) {
      if (_timedout) {
        return false;
      }
      _mmex = mmex;
      this.notify();
      return true;
    }

    synchronized MyRoleMessageExchange getResponse(long timeout) {
      long etime = timeout == 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
      long ctime;
      try {
        while (_mmex == null && (ctime = System.currentTimeMillis()) < etime) {
          this.wait(etime - ctime);
        }
      }
      catch (InterruptedException ie) {
        // ignore
      }
      _timedout = _mmex == null;
      return _mmex;
    }
  }

}
