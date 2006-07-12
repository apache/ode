package com.fs.pxe.axis;

import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.ContextException;
import com.fs.pxe.bpel.iapi.MessageExchangeContext;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.epr.WSDL11Endpoint;
import com.fs.pxe.bpel.epr.EndpointFactory;
import com.fs.utils.Namespaces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Implementation of the PXE {@link com.fs.pxe.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the PXE engine to make invocation of external
 * services using Axis.
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

  private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);

  private PXEServer _server;
  private AxisInvoker _invoker;

  public MessageExchangeContextImpl(PXEServer server) {
    _server = server;
    _invoker = _server.createInvoker();
  }

  public void invokePartner(PartnerRoleMessageExchange partnerRoleMessageExchange) throws ContextException {
    if (__log.isDebugEnabled())
      __log.debug("Invoking a partner operation: " + partnerRoleMessageExchange.getOperationName());

    EndpointReference epr = partnerRoleMessageExchange.getEndpointReference();
    // We only invoke with WSDL 1.1 service elements, that makes our life easier
    if (!(epr instanceof WSDL11Endpoint))
      epr = EndpointFactory.convert(new QName(Namespaces.WSDL_11, "service"), epr.toXML().getDocumentElement());
    // It's now safe to cast
    ExternalService service = _server.getExternalService(((WSDL11Endpoint)epr).getServiceName());
    service.invoke(partnerRoleMessageExchange);
  }

  public void onAsyncReply(MyRoleMessageExchange myRoleMessageExchange) throws BpelEngineException {
    if (__log.isDebugEnabled())
      __log.debug("Processing an async reply from service " + myRoleMessageExchange.getServiceName());
    PXEService service = _server.getService(myRoleMessageExchange.getServiceName());
    service.notifyResponse(myRoleMessageExchange);
  }
}
