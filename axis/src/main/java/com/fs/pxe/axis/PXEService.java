package com.fs.pxe.axis;

import com.fs.pxe.bpel.engine.BpelServerImpl;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.utils.DOMUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.transaction.TransactionManager;
import javax.wsdl.Part;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
  private Map<String,ResponseCallback> _waitingCallbacks;

  public PXEService(AxisService axisService, BpelServerImpl server, TransactionManager txManager) {
    _axisService = axisService;
    _server = server;
    _txManager = txManager;
    _waitingCallbacks = Collections.synchronizedMap(new HashMap<String, ResponseCallback>());
  }

  public void onAxisMessageExchange(MessageContext msgContext, MessageContext outMsgContext,
                                    SOAPFactory soapFactory) throws AxisFault {
    boolean success = false;
    MyRoleMessageExchange pxeMex = null;
    try {
      _txManager.begin();

      pxeMex = _server.getEngine().createMessageExchange(
              msgContext.getMessageID(),
              new QName(msgContext.getAxisService().getTargetNamespace(), msgContext.getAxisService().getName()),
              null,
              msgContext.getAxisOperation().getName().getLocalPart());
      if (pxeMex.getOperation() != null) {
        javax.wsdl.Message msgdef = pxeMex.getOperation().getInput().getMessage();
        Message pxeRequest = pxeMex.createMessage(pxeMex.getOperation().getInput().getMessage().getQName());
        convertMessage(msgdef, pxeRequest, msgContext.getEnvelope().getBody().getFirstElement());

        // Preparing a callback just in case we would need one.
        ResponseCallback callback = null;
        if (pxeMex.getOperation().getOutput() != null) {
          callback = new ResponseCallback();
          _waitingCallbacks.put(pxeMex.getMessageExchangeId(), callback);
        }

        if (__log.isDebugEnabled())
          __log.debug("Invoking PXE using MEX " + pxeMex);
        pxeMex.invoke(pxeRequest);

        // Invocation response could be delayed, if so we have to wait for it.
        if (pxeMex.getStatus() == MessageExchange.Status.ASYNC) {
          pxeMex = callback.getResponse(TIMEOUT);
        } else {
          // Callback wasn't necessary, cleaning up
          _waitingCallbacks.remove(pxeMex.getMessageExchangeId());
        }

        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        outMsgContext.setEnvelope(envelope);

        // Hopefully we have a response
        __log.debug("Handling response for MEX " + pxeMex);
        onResponse(pxeMex, envelope);

        success = true;
      } else {
        __log.error("PXE MEX " + pxeMex + " was unroutable.");
      }
    } catch(Exception e) {
      throw new AxisFault("An exception occured when invoking PXE.", e);
    } finally {
      if (success) {
        __log.debug("Commiting PXE MEX "  + pxeMex );
        try {
          _txManager.commit();
        } catch (Exception e) {
          throw new AxisFault("Commit failed!", e);
        }
      } else {
        __log.debug("Rolling back PXE MEX "  + pxeMex );
        try {
          _txManager.rollback();
        } catch (Exception e) {
          throw new AxisFault("Rollback failed!", e);
        }
      }
    }
    if (!success) throw new AxisFault("Message was unroutable!");
  }

  public void notifyResponse(MyRoleMessageExchange mex) {
    ResponseCallback callback = _waitingCallbacks.get(mex.getMessageExchangeId());
    if (callback == null) {
      __log.error("No active service for message exchange: "  + mex);
    } else {
      callback.onResponse(mex);
      _waitingCallbacks.remove(mex.getMessageExchangeId());
    }
  }

  private void onResponse(MyRoleMessageExchange mex, SOAPEnvelope envelope) throws AxisFault {
    switch (mex.getStatus()) {
      case FAULT:
        throw new AxisFault(null, mex.getFault(), null, null, OMUtils.toOM(mex.getFaultResponse().getMessage()));
      case RESPONSE:
        fillEnvelope(mex.getResponse(), envelope);
        break;
      case FAILURE:
        // TODO: get failure codes out of the message.
        throw new AxisFault("Message exchange failure!");
      default :
        __log.warn("Received PXE message exchange in unexpected state: " + mex.getStatus());
    }
  }

  private void convertMessage(javax.wsdl.Message msgdef, Message dest, OMElement body) throws AxisFault {
    Element srcel = OMUtils.toDOM(body);

    Document pxemsgdoc = DOMUtils.newDocument();
    Element pxemsg = pxemsgdoc.createElement("message");
    pxemsgdoc.appendChild(pxemsg);

    List<Part> expectedParts = msgdef.getOrderedParts(null);

    Element srcpart = DOMUtils.getFirstChildElement(srcel);
    for (Part pdef : expectedParts) {
      Element p = pxemsgdoc.createElement(pdef.getName());
      pxemsg.appendChild(p);
      if (srcpart != null) {
        NodeList nl = srcpart.getChildNodes();
        for (int j = 0; j < nl.getLength(); ++j)
          p.appendChild(pxemsgdoc.importNode(nl.item(j), true));
        srcpart = DOMUtils.getNextSiblingElement(srcpart);
      } else {
        __log.error("Improperly formatted message, missing part: " + pdef.getName());
      }
    }

    dest.setMessage(pxemsg);
  }

  private void fillEnvelope(Message resp, SOAPEnvelope envelope) throws AxisFault {
    Element srcPartEl = DOMUtils.getFirstChildElement(resp.getMessage());
    while (srcPartEl != null) {
      envelope.getBody().addChild(OMUtils.toOM(srcPartEl));
      srcPartEl = DOMUtils.getNextSiblingElement(srcPartEl);
    }
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
