/*
 * File:      $Id: HttpSoapService.java 1506 2006-06-21 17:02:05Z mbs $
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import com.fs.pxe.sfwk.core.ServiceEndpoint;
import com.fs.pxe.sfwk.core.StatefulServiceEndpoint;
import com.fs.pxe.sfwk.spi.*;
import com.fs.pxe.soap.mapping.*;
import com.fs.utils.DOMUtils;
import com.fs.utils.Namespaces;
import com.fs.utils.msg.MessageBundle;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.transaction.*;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A SOAP "adapter" service.
 */
class HttpSoapService extends HttpSoapEventHandler {
  private static final Messages __msgs = MessageBundle.getMessages(Messages.class);
  private static final Log __log = LogFactory.getLog(HttpSoapService.class);

  private HttpSoapAdapter _adapter;

  /** WSDL details regarding this service. */
  private SoapServiceInfo _serviceInfo;

  private TransactionManager _txm;
  private SoapServiceInfo.PortInfo _portInfo;
  private MultiThreadedHttpConnectionManager _connectionManager;
  private HttpClient _httpClient;
  private Executor _threadPool;

  /** The backup queue that we use when we exceed the max number of connections. */
  private final Queue<DoInvoke> _invokeQueue = new ConcurrentLinkedQueue<DoInvoke>();

  /** Number of threads that are active. */
  private int _numActive;

  /** Maximum number of active HTTP connection threads. */
  private int _maxActive = 25;

  /** Maximum number of items in queue before we start sending back mex failures. */
  private int _maxQueueLength = 100;

  HttpSoapService(HttpSoapAdapter adapter, TransactionManager txm, ServiceContext service) throws ServiceProviderException {
    super(__log);
    _adapter = adapter;
    _serviceInfo = new SoapServiceInfo(service);
    _txm = txm;
    _connectionManager = new MultiThreadedHttpConnectionManager();
    _threadPool = adapter._context.getExeuctorService();
    HttpConnectionManagerParams params = _connectionManager.getParams();
    params.setMaxTotalConnections(_maxActive);
    params.setMaxConnectionsPerHost(HostConfiguration.ANY_HOST_CONFIGURATION, _maxActive);
    _httpClient = new HttpClient(_connectionManager);
  }

  SoapBindingModel getBindingModel(String port) {
    return _serviceInfo.getAdapterMapper(port);
  }

  SoapServiceInfo getInfo() {
    return _serviceInfo;
  }

  void deactivate() {
    _connectionManager.shutdown();
  }

  void onMessageExchangeEvent(MessageExchangeEvent mexevent) throws MessageExchangeException {
    if (mexevent.isServerEvent()) {
      onMessageExchangeEventServer(mexevent);
    }
    else if (mexevent.isClientEvent()) {
      onMessageExchangeEventClient(mexevent);
    }
  }

  private void onMessageExchangeEventServer(MessageExchangeEvent messageExchangeEvent)
          throws MessageExchangeException
  {
    MessageExchange ex = messageExchangeEvent.getMessageExchange();
    if (_log.isDebugEnabled()) {
      _log.debug("onMessageExchangeEventServer: " + messageExchangeEvent );
    }
    switch (messageExchangeEvent.getEventType()) {

      case MessageExchangeEvent.RECOVER:
        // It would be safest to fail, we really don't know if the call succeeded
        // though.
        ex.failure(__msgs.msgInvokeHazard(ex.getInstanceId()));
        break;

      case MessageExchangeEvent.IN_RCVD_EVENT:
        _portInfo = _serviceInfo.getConnectorPort(messageExchangeEvent.getPort().getPortName());
        assert _portInfo != null;

        synchronized (_invokeQueue) {
          DoInvoke doInvoke = new DoInvoke(ex.getReference());
          if (_invokeQueue.isEmpty() && _numActive < _maxActive) {
            ++_numActive;
            _threadPool.execute(doInvoke);
          } else if (_invokeQueue.size() < _maxQueueLength) {
            _invokeQueue.add(doInvoke);
          } else {
            ex.failure("Connection backup. ");
          }
        }
        break;

      case MessageExchangeEvent.FAILURE:
        // Ignore.
        break;

      default:
        if (_log.isDebugEnabled()) {
          _log.debug("Unexpected messageExchangeEvent: " + messageExchangeEvent + " (IGNORING).");
        }
        break;
    }

  }

  /**
   * Process message exchange event for this service.
   * @param messageExchangeEvent
   */
  private void onMessageExchangeEventClient(MessageExchangeEvent messageExchangeEvent)  {
    if (_log.isDebugEnabled()) {
      _log.debug("onMessageExchangeEventClient: " + messageExchangeEvent );
    }
    MessageExchange mex = messageExchangeEvent.getMessageExchange();

    ServicePort port = messageExchangeEvent.getPort();

    SoapBindingModel mapper = getBindingModel(port.getPortName());
    assert mapper != null;

    boolean isFault = false;
    String faultName = null;
    Message msg = null;
    switch (messageExchangeEvent.getEventType()) {

      case MessageExchangeEvent.OUT_FAULT_EVENT:
      msg = ((OutFaultRcvdMessageExchangeEvent)messageExchangeEvent).getOutFaultMessage();
      faultName = ((OutFaultRcvdMessageExchangeEvent)messageExchangeEvent).getFaultName();
      isFault = true;
      break;

      case MessageExchangeEvent.OUT_RCVD_EVENT:
        msg = ((OutputRcvdMessageExchangeEvent)messageExchangeEvent).getOutputMessage();
        break;

      case MessageExchangeEvent.FAILURE:
        // TODO: better handling of failures.
        msg = null;
        isFault = true;
        break;

      case MessageExchangeEvent.RECOVER:
        // We don't need to do anything special for our
        // recovery, we expect the server side to do the recovery
        return;

      default:
        return;
    }

    String operation = messageExchangeEvent.getMessageExchange().getName();
    SoapOperationBindingModel opBinding = mapper.getOperation(operation);
    SOAPWriter responseWriter = isFault ? opBinding.getSoapResponseWriter(faultName) : opBinding.getSoapResponseWriter();

    HttpSoapResponse response = new HttpSoapResponse();

    if (isFault && msg == null) {
      // FAILURE case.
      createFault(response, FAULT_SERVER, "MessageExchange Failure", null);
      _adapter.sendResponse(mex, response);
      return;
    }

    Map<String, Element> partVals = new HashMap<String, Element>();
    for (Iterator i = msg.getDescription().getParts().keySet().iterator(); i.hasNext(); ) {
      String partName = (String) i.next();
      partVals.put(partName, msg.getPart(partName));
    }

    Document soapResponse;
    try {
      soapResponse = DOMUtils.newDocument();
      responseWriter.write(soapResponse, partVals);
    } catch (SoapFormatException soapEx) {
      // This is a bit unusual. We should be able to generate SOAP from anything PXE produces.
      String errmsg = "Unexpected error mapping PXE message to SOAP; contact FiveSight Technical Support!";
      _log.fatal(errmsg, soapEx);
      createFault(response, FAULT_SERVER, errmsg, soapEx);
      _adapter.sendResponse(mex, response);
      return;
    }

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      new SoapMessage(soapResponse).writeTo(bos);
      bos.close();
    } catch (Exception e) {
      // Odd, IO error is a bit unexpected
      String errmsg = "Unexpected I/O error mapping PXE message to SOAP; sending server error response!";
      _log.fatal(errmsg, e);
      createFault(response, FAULT_SERVER, errmsg, e);
      _adapter.sendResponse(mex, response);
      return;
    }

    response.setPayload(bos.toByteArray());
    response.setHeader("SOAPAction", opBinding.getSOAPAction() == null ? "" : opBinding.getSOAPAction());
    response.setStatus(isFault ? 500 : 200);
    _adapter.sendResponse(mex, response);
  }

  /**
   * Schedules work from our local queue.
   */
  private void schedule() {
    do {
      if (_invokeQueue.isEmpty() || _numActive >= _maxActive) {
        return;
      }
      DoInvoke doInvoke = _invokeQueue.poll();
      _threadPool.execute(doInvoke);
    }
    while (true);
  }

  private boolean forceFailure(MessageExchangeRef ref, String errmsg) {
    try {
      _txm.begin();
      MessageExchange mex = ref.resolve();
      mex.failure(errmsg);
      mex.release();
      _txm.commit();
      return true;
    } catch (Exception ex) {
      _log.debug("forceFailure failed: " + ex, ex);
      return false;
    } finally {
      try {
        if (_txm.getStatus() != Status.STATUS_NO_TRANSACTION)
          _txm.rollback();
      } catch (Exception ex) {
        _log.debug("forceFailure TX cleanup failed: " + ex, ex);
      }
    }
  }

  /**
   * {@link Runnable} that actuall performs the SAAJ invocation. We keep this
   * in a seperate thread because it is not transactional and can take a long
   * time.
   */
  private class DoInvoke implements Runnable {
    private MessageExchangeRef _ref;
    private transient PostMethod _httpPostMethod;
    private transient String _soapAction;
    private transient boolean _twoWay;
    private transient SoapOperationBindingModel _opbinding;

    DoInvoke(MessageExchangeRef mexRef)  {
      _ref = mexRef;
    }

    private void buildRequest() throws Exception {
      _txm.begin();
      try {
        MessageExchange mex = _ref.resolve();
        _twoWay = mex.getPortType().getOperation(mex.getName(), null, null).getOutput() != null;
        Message pxeRequestMsg = mex.lastInput();
        _opbinding = _portInfo.getSoapBindingModel().getOperation(mex.getOperation().getName());

        SOAPWriter writer = _opbinding.getSoapRequestWriter();
        _soapAction = writer.getSoapAction();

        Map<String, Element> allParts = new HashMap<String, Element>(pxeRequestMsg.getParts());
        String soapUrl = handleHeaderParts(mex, allParts);
        _httpPostMethod = new PostMethod(soapUrl);
        Document dest = DOMUtils.newDocument();
        writer.write(dest, allParts);
        if (_soapAction != null) {
          _httpPostMethod.setRequestHeader("SOAPAction", _soapAction);
        }
        _httpPostMethod.setRequestHeader("Content-Type", "text/xml");
        _httpPostMethod.setRequestEntity(new StringRequestEntity(DOMUtils.domToString(dest.getDocumentElement())));
        _txm.commit();
      } finally {
        if (_txm.getStatus() != Status.STATUS_NO_TRANSACTION)
          _txm.rollback();
      }
    }

    private void doTwoWayInvoke() throws
            SystemException, NotSupportedException, HeuristicMixedException, HeuristicRollbackException, RollbackException, MessageExchangeException {
      SoapMessage responseMsg;
      try {
        if(_log.isDebugEnabled()) {
          _log.debug("INVOKE2 " + _ref.getInstanceId() + " on " + _httpPostMethod.getURI());
        }
        _httpClient.executeMethod(_httpPostMethod);
        if (_log.isDebugEnabled()) {
          _log.debug("INVOKE2 " + _ref.getInstanceId() + " on " + _httpPostMethod.getURI() +  ": " + _httpPostMethod.getStatusCode()
                  + " - " + _httpPostMethod.getStatusText());
        }
        if (_httpPostMethod.getStatusCode() == 200 || _httpPostMethod.getStatusCode() == 500) {
          InputStream responseStream = _httpPostMethod.getResponseBodyAsStream();
          responseMsg = new SoapMessage(responseStream);
        } else {
          responseMsg = null;
        }
      } catch (SAXException se) {
        _txm.begin();
        MessageExchange mex = _ref.resolve();
        String errmsg = __msgs.msgResponseFormatError(mex.getInstanceId(), _portInfo.getSoapURL());
        _log.error(errmsg, se);
        mex.failure(errmsg);
        _txm.commit();
        return;
      } catch (SoapFormatException sfe) {
        String errmsg = __msgs.msgResponseFormatError(_ref.getInstanceId(), _portInfo.getSoapURL());
        _log.error(errmsg, sfe);
        _txm.begin();
        MessageExchange mex = _ref.resolve();
        mex.failure(errmsg);
        _txm.commit();
        return;
      } catch (IOException ex) {
        // If we cannot hit the target, we fail the exchange. If we want to
        // implement retry policies, then we should not do here, but rathr
        // at the sfwk layer.
        _txm.begin();
        MessageExchange mex = _ref.resolve();
        String errmsg = __msgs.msgHttpInvokeError(mex.getInstanceId(), _portInfo.getSoapURL());
        _log.error(errmsg, ex);
        mex.failure(errmsg);
        _txm.commit();
        return;
      }

      // Ok, we did the request, and got something resembling a good response!
      try {
        _txm.begin();
        handleResponse(responseMsg);
        _txm.commit();
      } catch (MessageExchangeException ex) {
        // Ah, this is a problem, we really need to recover this invocation at this point.
        _txm.rollback();
        _txm.begin();
        MessageExchange mex = _ref.resolve();
        mex.failure(ex.getMessage());
        _txm.commit();
      } finally {
        // Force rollback.
        if (_txm.getStatus() != Status.STATUS_NO_TRANSACTION) {
          _txm.rollback();
        }
      }
    }


    private void doOneWayInvoke() throws SystemException, NotSupportedException, HeuristicMixedException, HeuristicRollbackException, RollbackException, MessageExchangeException {
      try {
        if(_log.isDebugEnabled()) {
          _log.debug("SAAJINVOKE1 " + _ref.getInstanceId() + " on " + _httpPostMethod.getURI());
        }
        _httpClient.executeMethod(_httpPostMethod);

        if(_log.isDebugEnabled())
          _log.debug("SAAJINVOKE1 " + _ref.getInstanceId() + " on " + _httpPostMethod.getURI() + " - COMPLETED OK");
      } catch (IOException ex) {
        if(_log.isDebugEnabled()) {
          _log.debug("SAAJINVOKE1 " + _ref.getInstanceId() + " - ERROR", ex);
        }
        // If we cannot hit the target, we fail the exchange. If we want to
        // implement retry policies, then we should not do here, but rathr
        // at the sfwk layer.
        _txm.begin();
        MessageExchange mex = _ref.resolve();
        String errmsg = __msgs.msgHttpInvokeError(mex.getInstanceId(), _portInfo.getSoapURL());
        _log.error(errmsg, ex);
        mex.failure(errmsg);
        _txm.commit();
        return;
      }

      _txm.begin();
      MessageExchange mex = _ref.resolve();
      mex.release();
      _txm.commit();
    }

    private void handleResponse(SoapMessage responseMsg) throws MessageExchangeException {
      MessageExchange mex = _ref.resolve();
      if (responseMsg == null) {
        mex.failure(__msgs.msgNoResponseFailure(_httpPostMethod.getStatusCode(), _httpPostMethod.getStatusText()));
      } else if (responseMsg.isFault()) {
        Map<QName, Element> details = responseMsg.getFaultDetails();
        String faultName = _opbinding.findMatchingFault(details.keySet());
        if (faultName == null) {
          String errmsg = __msgs.msgFaultMappingError(mex.getInstanceId(), responseMsg.getFaultString(), _portInfo.getSoapURL());
          _log.error(errmsg);
          mex.failure(errmsg);
        } else {
          Message outfaultMsg = mex.createOutfaultMessage(faultName);
          SOAPReader soapFaultReader = _opbinding.getSoapResponseReader(faultName);
          String partName = soapFaultReader.getParts().iterator().next();
          Element partVal;
          try {
            partVal = soapFaultReader.readPart(responseMsg, partName);
          } catch (SoapFormatException sme) {
            partVal = null;
            String errmsg = __msgs.msgResponseMappingError(_ref.getInstanceId(), partName, _portInfo.getSoapURL());
            _log.error(errmsg, sme);
          }

          if (partVal == null) {
            String errmsg = __msgs.msgResponseMissingPart(_ref.getInstanceId(), partName, _portInfo.getSoapURL() );
            _log.error(errmsg);
            mex.failure(errmsg);
          } else {
            outfaultMsg.setPart(partName, partVal);
            mex.outfault(faultName, outfaultMsg);
          }
        }
      } else if (responseMsg.isValidResponse()) {
        Message msg = mex.createOutputMessage();
        SOAPReader reader = _opbinding.getSoapResponseReader();
        boolean allPartsFound = true;
        StringBuffer missingParts = new StringBuffer();
        for (String s : reader.getParts()) {
          String partName = (String) s;
          Element partVal;

          try {
            partVal = reader.readPart(responseMsg, partName);
          } catch (SoapFormatException sme) {
            partVal = null;
            String errmsg = __msgs.msgResponseMappingError(_ref.getInstanceId(), partName, _portInfo.getSoapURL());
            _log.error(errmsg, sme);
          }

          if (partVal == null) {
            String errmsg = __msgs.msgResponseMissingPart(_ref.getInstanceId(), partName, _portInfo.getSoapURL());
            _log.error(errmsg);
            if (!allPartsFound) {
              missingParts.append(", ");
            }
            missingParts.append(partName);
            allPartsFound = false;
            break;
          }

          msg.setPart(partName, partVal);
        }

        if (!allPartsFound) {
          String errmsg = __msgs.msgIncompleteResponse(_ref.getInstanceId(), missingParts.toString());
          mex.failure(errmsg);
        }

        try{
          msg.checkValid();
          mex.output(msg);
        }catch(MessageFormatException e){
          String errmsg = __msgs.msgRequestMappingError(_ref.getInstanceId());
          _log.error(errmsg, e);
          mex.failure(errmsg);
        }
      } else {
        mex.failure("Invalid response payload.");
      }

      mex.release();
    }


    public void run(){
      try {
        if (_log.isDebugEnabled()) {
          _log.debug("DoInvoke: " + _ref );
        }
        buildRequest();

        if (_twoWay) {
          doTwoWayInvoke();
        }
        else {
          doOneWayInvoke();
        }
      } catch (Throwable t) {
        _log.error(t.getMessage(), t);
        // We experienced a problem, need to make sure to try failing the exchange.
        for (int i = 0; i < 5; ++i) {
          if (forceFailure(_ref, "Internal System Error: " + t.toString())) {
            return;
          }
          try { Thread.currentThread().wait(5000); } catch (Exception ex) {}
        }
        _log.fatal("Unable to mark message exchange as failed (very serious): " + _ref.getInstanceId(), t);
      } finally {
        if(_httpPostMethod != null) {
        	_httpPostMethod.releaseConnection();
        }
        synchronized (_invokeQueue) {
          --_numActive;
        }
        schedule();
      }
    }

    private String handleHeaderParts(MessageExchange mex, Map<String, Element> parts) {
      String soapUrl;
      if (mex.getDestinationServiceEndpoint() != null) {
        ServiceEndpoint urlToEpr = mex.getDestinationServiceEndpoint();
        Document doc = DOMUtils.newDocument();

        Element wsaToElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "To");
        wsaToElmt.setTextContent(urlToEpr.getUrl());
        parts.put("wsaTo", wsaToElmt);

        Element wsaActionElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Action");
        if (_soapAction == null) {
          QName ptQName = _portInfo.getPort().getPortType().getQName();
          String actionUrl = ptQName.getNamespaceURI() + (ptQName.getNamespaceURI().endsWith("/") ? "" : "/")
                  + ptQName.getLocalPart() + "/" + _opbinding.getOperation().getName() + "/" +
                  _opbinding.getRequestMessage().getQName().getLocalPart();
          wsaActionElmt.setTextContent(actionUrl);
        } else {
          wsaActionElmt.setTextContent(_soapAction);
        }
        parts.put("wsaAction", wsaActionElmt);

        soapUrl = urlToEpr.getUrl();
        if (urlToEpr instanceof StatefulServiceEndpoint) {
          StatefulServiceEndpoint sessToEpr = (StatefulServiceEndpoint) urlToEpr;
          Element intalioSessElmt = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "identifier");
          intalioSessElmt.setTextContent(sessToEpr.getSessionId());
          parts.put("sessionId", intalioSessElmt);
        }
      } else {
        soapUrl = _portInfo.getSoapURL().toExternalForm();
      }
      if (mex.getSourceServiceEndpoint() != null) {
        StatefulServiceEndpoint sessFromEpr = (StatefulServiceEndpoint) mex.getSourceServiceEndpoint();
        Document doc = DOMUtils.newDocument();
        Element callbackElmt = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "callback");
        Element addressElmt = doc.createElementNS(Namespaces.WS_ADDRESSING_NS, "Address");
        Element sessionIdElmt = doc.createElementNS(Namespaces.INTALIO_SESSION_NS, "identifier");
        addressElmt.setTextContent(sessFromEpr.getUrl());
        sessionIdElmt.setTextContent(sessFromEpr.getSessionId());
        doc.appendChild(callbackElmt);
        callbackElmt.appendChild(addressElmt);
        callbackElmt.appendChild(sessionIdElmt);
        parts.put("fromEndpoint", callbackElmt);
      }
      return soapUrl;
    }
  }

}
