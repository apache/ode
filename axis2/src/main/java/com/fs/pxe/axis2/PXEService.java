package com.fs.pxe.axis2;

import com.fs.pxe.bpel.engine.BpelServerImpl;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.epr.WSAEndpoint;
import com.fs.pxe.bom.wsdl.Definition4BPEL;
import com.fs.utils.DOMUtils;
import com.fs.utils.GUID;
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
public class PXEService {

  private static final Log __log = LogFactory.getLog(PXEService.class);
  private static final int TIMEOUT = 2 * 60 * 1000;

  private AxisService _axisService;
  private BpelServerImpl _server;
  private TransactionManager _txManager;
  private Definition _wsdlDef;
  private QName _serviceName;
  private String _portName;
  private Map<String,ResponseCallback> _waitingCallbacks;

  public PXEService(AxisService axisService, Definition4BPEL def, QName serviceName, String portName,
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
    MyRoleMessageExchange pxeMex = null;
    ResponseCallback callback = null;
    try {
      _txManager.begin();

      // Creating mesage exchange
      String messageId = new GUID().toString();
      pxeMex = _server.getEngine().createMessageExchange(""+messageId, _serviceName, null,
              msgContext.getAxisOperation().getName().getLocalPart());
      __log.debug("PXE routed to operation " + pxeMex.getOperation() + " from service " + _serviceName);

      if (pxeMex.getOperation() != null) {
        // Preparing message to send to PXE
        Message pxeRequest = pxeMex.createMessage(pxeMex.getOperation().getInput().getMessage().getQName());
        Element msgContent = SOAPUtils.unwrap(OMUtils.toDOM(msgContext.getEnvelope().getBody().getFirstElement()),
                _wsdlDef, pxeMex.getOperation().getInput().getMessage(), _serviceName);
        readHeader(msgContext, pxeMex);
        pxeRequest.setMessage(msgContent);

        // Preparing a callback just in case we would need one.
        if (pxeMex.getOperation().getOutput() != null) {
          callback = new ResponseCallback();
          _waitingCallbacks.put(pxeMex.getClientId(), callback);
        }

        if (__log.isDebugEnabled()) {
          __log.debug("Invoking PXE using MEX " + pxeMex);
          __log.debug("Message content:  " + DOMUtils.domToString(pxeRequest.getMessage()));
        }
        // Invoking PXE
        pxeMex.invoke(pxeRequest);
      } else {
        success = false;
      }
    } catch(Exception e) {
      e.printStackTrace();
      success = false;
      throw new AxisFault("An exception occured when invoking PXE.", e);
    } finally {
      if (success) {
        __log.debug("Commiting PXE MEX "  + pxeMex );
        try {
          _txManager.commit();
        } catch (Exception e) {
          __log.error("COMMIT FAILED!", e);
          success = false;
        }
      }
      if (!success) {
        __log.error("Rolling back PXE MEX "  + pxeMex );
        try {
          _txManager.rollback();
        } catch (Exception e) {
          throw new AxisFault("ROLLBACK FAILED!", e);
        }
      }

      if (pxeMex.getOperation() != null) {
        boolean timeout = false;
        // Invocation response could be delayed, if so we have to wait for it.
        if (pxeMex.getStatus() == MessageExchange.Status.ASYNC) {
          pxeMex = callback.getResponse(TIMEOUT);
          if (pxeMex == null) timeout = true;
        } else {
          // Callback wasn't necessary, cleaning up
          _waitingCallbacks.remove(pxeMex.getMessageExchangeId());
        }

        if (outMsgContext != null) {
          SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
          outMsgContext.setEnvelope(envelope);

          // Hopefully we have a response
          __log.debug("Handling response for MEX " + pxeMex);
          if (timeout) {
            __log.error("Timeout when waiting for response to MEX " + pxeMex);
            success = false;
          } else {
            try {
              _txManager.begin();
              onResponse(pxeMex, outMsgContext);
            } catch (Exception e) {
              try {
                _txManager.rollback();
              } catch (Exception ex) {
                throw new AxisFault("Rollback failed!", ex);
              }
              throw new AxisFault("An exception occured when invoking PXE.", e);
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
        __log.warn("Received PXE message exchange in unexpected state: " + mex.getStatus());
    }
  }

  /**
   * Extracts endpoint information from Axis MessageContext (taken from WSA headers) to
   * stuff them into PXE mesage exchange.
   */
  private void readHeader(MessageContext msgContext, MyRoleMessageExchange pxeMex) {
    Object otse = msgContext.getProperty("targetSessionEndpoint");
    Object ocse = msgContext.getProperty("callbackSessionEndpoint");
    if (otse != null) {
      Element serviceEpr = (Element) otse;
      WSAEndpoint endpoint = new WSAEndpoint();
      endpoint.set(serviceEpr);
      pxeMex.setEndpointReference(endpoint);
    }
    if (ocse != null) {
      Element serviceEpr = (Element) ocse;
      WSAEndpoint endpoint = new WSAEndpoint();
      endpoint.set(serviceEpr);
      pxeMex.setCallbackEndpointReference(endpoint);
    }
  }

  /**
   * Extracts endpoint information from PXE message exchange to stuff them into
   * Axis MessageContext.
   */
  private void writeHeader(MessageContext msgContext, MyRoleMessageExchange pxeMex) {
    if (pxeMex.getEndpointReference() != null) {
      msgContext.setProperty("targetSessionEndpoint", pxeMex.getEndpointReference());
      msgContext.setProperty("soapAction",
              SOAPUtils.getSoapAction(_wsdlDef, _serviceName, _portName, pxeMex.getOperationName()));
    }
    if (pxeMex.getCallbackEndpointReference() != null) {
      msgContext.setProperty("callbackSessionEndpoint", pxeMex.getCallbackEndpointReference());
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
