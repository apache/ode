package com.fs.pxe.axis;

import com.fs.pxe.bpel.engine.BpelServerImpl;
import com.fs.pxe.bpel.iapi.Message;
import com.fs.pxe.bpel.iapi.MessageExchange;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.utils.DOMUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * A running service, encapsulates the Axis service, its receivers and
 * our receivers as well.
 */
public class PXEService {

  private static final Log __log = LogFactory.getLog(PXEService.class);

  private AxisService _axisService;
  private BpelServerImpl _server;
  private TransactionManager _txManager;

  public PXEService(AxisService axisService, BpelServerImpl server, TransactionManager txManager) {
    _axisService = axisService;
    _server = server;
    _txManager = txManager;
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

        pxeMex.invoke(pxeRequest);

        // Handle the response if it is immediately available.
        if (pxeMex.getStatus() != MessageExchange.Status.ASYNC && outMsgContext != null) {
          __log.debug("PXE MEX "  + pxeMex  + " completed SYNCHRONOUSLY.");
          SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
          outMsgContext.setEnvelope(envelope);
          onResponse(pxeMex, envelope);
        } else {
          __log.debug("PXE MEX " + pxeMex + " completed ASYNCHRONOUSLY.");
        }
        success = true;
      } else {
        __log.error("PXE MEX "  +pxeMex + " was unroutable.");
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

  private void onResponse(MyRoleMessageExchange mex, SOAPEnvelope envelope) throws AxisFault {
    switch (mex.getStatus()) {
      case FAULT:
        throw new AxisFault(null, mex.getFault(), null, null, toOM(mex.getFaultResponse().getMessage()));
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
    Element srcel = toDOM(body);

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
      envelope.getBody().addChild(toOM(srcPartEl));
      srcPartEl = DOMUtils.getNextSiblingElement(srcPartEl);
    }
  }

  private Element toDOM(OMElement element) throws AxisFault {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      element.serialize(baos);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return DOMUtils.parse(bais).getDocumentElement();
    } catch (Exception e) {
      throw new AxisFault("Unable to read Axis input messag.e", e);
    }
  }

  private OMElement toOM(Element element) throws AxisFault {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try {
      DOMUtils.serialize(element, baos);
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(bais);
      StAXOMBuilder builder = new StAXOMBuilder(parser);
      return builder.getDocumentElement();
    } catch (Exception e) {
      throw new AxisFault("Unable to read Axis input messag.e", e);
    }
  }

}
