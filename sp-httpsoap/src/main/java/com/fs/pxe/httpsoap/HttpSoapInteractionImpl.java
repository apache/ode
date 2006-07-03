/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.pxe.sfwk.spi.*;
import com.fs.pxe.soap.mapping.*;
import com.fs.utils.DOMUtils;
import com.fs.utils.Namespaces;
import org.apache.commons.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.transaction.TransactionManager;
import java.io.IOException;


/**
 * Handles client invocations for sending SOAPMessages as request, and
 * request-response.
 */
class HttpSoapInteractionImpl extends HttpSoapEventHandler
        implements HttpSoapInteraction, InteractionHandler {

  private TransactionManager _tx;
  private HttpSoapAdapter _adapterInstance;

  HttpSoapInteractionImpl(TransactionManager txm, Log log, HttpSoapAdapter adapterInstance)
          throws ServiceProviderException {
    super(log);
    _tx = txm;
    _adapterInstance = adapterInstance;
  }

  /**
   * @see com.fs.pxe.httpsoap.HttpSoapInteraction#handleHttpSoapRequest(com.fs.pxe.httpsoap.HttpSoapRequest, long)
   */
  public HttpSoapResponse handleHttpSoapRequest(HttpSoapRequest request, long timeout) throws IOException {

    if (_log.isDebugEnabled()) {
      _log.debug("handleHttpRequest(request=" + request +")");
    }

    HttpSoapResponse response = new HttpSoapResponse();
    response.setStatus(404);
    response.setErrorText("Not Found");
    response.setHeader("Content-Type", "text/xml");

    SoapServiceInfo.PortInfo portInfo = _adapterInstance.findPortInfo(request.getRequestUri());
    if (portInfo == null) {
      response.setErrorText("Unknown SOAP URI: " + request.getRequestUri());
      _log.error(response.getErrorText());
      return response;
    }

    SoapBindingModel bindingModel = portInfo.getSoapBindingModel();
    assert bindingModel != null;

    // Check for valid method types.
    if (!request.getAction().equals("POST")) {
      response.setStatus(HTTPERR_METHOD_NOT_ALLOWED);
      response.setErrorText("The method " + request.getAction() + " is not allowed; try POST instead.");
      _log.error(response.getErrorText());
      return response;
    }

    String soapAction = request.getHeader("SOAPAction");

    SoapMessage soapMessage;
    try {
      soapMessage = new SoapMessage(request.getPayloadInputStream());
    } catch (SoapFormatException e) {
      response.setStatus(HTTPERR_BAD_REQUEST);
      response.setErrorText("Invalid SOAP payload: " + e.getMessage());
      _log.error(response.getErrorText());
      return response;
    } catch (Exception ex) {
      response.setStatus(HTTPERR_BAD_REQUEST);
      response.setErrorText("Unable to parse request: " + ex.getMessage());
      _log.error(response.getErrorText());
      return response;
    }

    SoapOperationBindingModel soapOpBinding = null;
    try {
      soapOpBinding = bindingModel.findOperationBindingModel(soapAction, soapMessage.getPayloadQName());
    } catch (SoapFormatException sme) {
      // ignore, see below
    }

    if (soapOpBinding == null) {
      String msg = "Unroutable message; (" + soapAction  + ", " + soapMessage.getPayloadQName() +") is not a recognized (SOAPAction,Payload QNAME) pair," ;
      createFault(response,FAULT_CLIENT,msg, null);
      _log.error(response.getErrorText());
      return response;
    }

    // response soapMessage; will be null for one-way invocation
    try {
      _tx.begin();
    } catch (Exception e) {
      String msg = "Internal server error; unable to begin transaction!";
      createFault(response,FAULT_SERVER,msg, e);
      _log.error(msg, e);
      return response;
    }

    boolean success = false;
    MessageExchange me = null;
    HttpSoapResponseCallback callback = new HttpSoapResponseCallback();

    Element toEprELmt = null;
    Element fromEprELmt = null;
    // Getting EPRs provided by sender and session information if they exist.
    if (soapMessage.getSoapHeader() != null) {
      NodeList wsaToSessionList = soapMessage.getSoapHeader()
              .getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "identifier");
      if (wsaToSessionList.getLength() > 0) {
        Document doc = DOMUtils.newDocument();
        Element serviceEpr = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
        doc.appendChild(serviceEpr);
        serviceEpr.appendChild(doc.importNode(wsaToSessionList.item(0), true));
        NodeList wsaToAddressList = soapMessage.getSoapHeader()
                .getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "To");
        if (wsaToAddressList.getLength() > 0) {
          Element addressElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
          addressElmt.setTextContent(wsaToAddressList.item(0).getTextContent());
          serviceEpr.appendChild(addressElmt);
        }
        toEprELmt = serviceEpr;
      }

      NodeList fromEprList = soapMessage.getSoapHeader()
              .getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "callback");
      if (fromEprList.getLength() > 0) {
        Element fromEpr = (Element) fromEprList.item(0);
        NodeList intalioSession = fromEpr.getElementsByTagNameNS(Namespaces.INTALIO_SESSION_NS, "identifier");
        if (intalioSession.getLength() > 0) {
          Document doc = DOMUtils.newDocument();
          Element serviceEpr = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "EndpointReference");
          doc.appendChild(serviceEpr);
          serviceEpr.appendChild(doc.importNode(intalioSession.item(0), true));
          NodeList wsaFromAddressList = soapMessage.getSoapHeader()
                  .getElementsByTagNameNS(Namespaces.WS_ADDRESSING_NS, "Address");
          if (wsaFromAddressList.getLength() > 0) {
            serviceEpr.appendChild(doc.importNode(wsaFromAddressList.item(0), true));
          }
          fromEprELmt = serviceEpr;
        }
      }
    }

    try {
      ServicePort svcPort = portInfo.getPort();
      Message pxemsg;
      try {
        ServiceEndpoint fromSe = portInfo.getSoapServiceInfo().getService()
                .createServiceEndpoint(fromEprELmt);
        ServiceEndpoint toSe = portInfo.getSoapServiceInfo().getService()
                .createServiceEndpoint(toEprELmt);
        me = portInfo.getSoapServiceInfo().getService()
                .createMessageExchange(svcPort, fromSe, toSe, soapOpBinding.getOperation().getName(),
                        _adapterInstance.getInstanceCorrelationId());
        pxemsg = me.createInputMessage();
        pxemsg.setToEndpoint(toEprELmt);
        pxemsg.setFromEndpoint(fromEprELmt);
      } catch (NoSuchOperationException e1) {
        // Should not really happen.
        String msg = "Internal error.";
        createFault(response, FAULT_SERVER, msg, e1);
        _log.error(response.getErrorText(),e1);
        return response;
      } catch (MessageExchangeException e) {
        String msg = "SOAP server error.";
        createFault(response, FAULT_CLIENT, msg, e);
        _log.error(response.getErrorText(),e);
        return response;
      }

      SOAPReader requstReader = soapOpBinding.getSoapRequestReader();
      for (Object o : pxemsg.getDescription().getParts().keySet()) {
        String partName = (String) o;
        try {
          Element partVal = requstReader.readPart(soapMessage, partName);
          pxemsg.setPart(partName, partVal);
        } catch (MessageFormatException iae) {
          // Occurs when the message does not contain the required parts, or contains too many parts.
          String msg = "Invalid SOAP request: " + iae.getMessage();
          createFault(response, FAULT_CLIENT, msg, iae);
          response.setErrorText(msg);
          _log.error(response.getErrorText(), iae);
          return response;
        } catch (SoapFormatException sme) {
          // Occurs when the message does not contain the required parts, or contains too many parts.
          String msg = "Invalid SOAP request.";
          createFault(response, FAULT_CLIENT, msg, sme);
          response.setErrorText(msg);
          _log.error(response.getErrorText(), sme);
          return response;

        }
      }

      try{
        pxemsg.checkValid();
      }catch(MessageFormatException e){
        String msg = "Invalid SOAP request.";
        createFault(response, FAULT_CLIENT, msg, e);
        _log.error(msg, e);
        return response;
      }

      try {
        me.input(pxemsg);
      } catch (Exception ex) {
        String msg = "Message Exchange Error";
        createFault(response, FAULT_SERVER, msg, ex);
        _log.error(response.getErrorText(),ex);
        return response;
      }

      success = true;
    } finally {
      if (success) {
        try {
          assert me != null;
          if(!soapOpBinding.isOneWay())
            _adapterInstance.registerCallback(me.getInstanceId(), callback);
          _tx.commit();
        } catch (Exception ex) {
          String msg = "PXE Server Error (Unable to commit transaction)";
          _log.error(msg,ex);
          try {
            _tx.rollback();
          } catch (Exception ex1) {
            // ignore
          }
          createFault(response, FAULT_SERVER, msg, ex);
          return response;
        }
      } else {
        try {
          _tx.rollback();
        } catch (Exception ex) {
          _log.error("Transaction rollback failed.",ex);
        }
      }
    }

    // If one way, just send back a HTTP ack.
    if (soapOpBinding.isOneWay()) {
      response = new HttpSoapResponse();
      response.setStatus(200);
      return response;
    }

    response = callback.getResponse(timeout);
    if (response == null) {
      response = new HttpSoapResponse();
      createFault(response, FAULT_SERVER, "Timeout waiting for response; operation may complete in the future.", null);
    }
    return response;
  }

  public void close() {
    // nothing to do really..
  }

}
