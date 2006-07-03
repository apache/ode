/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.xcontrib.sp.rpc;

import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.DOMUtils;
import org.apache.commons.logging.Log;
import org.xml.sax.SAXException;

import javax.transaction.TransactionManager;
import javax.wsdl.Operation;
import java.io.IOException;
import java.util.Iterator;


/**
 * Implementation of the {@link RpcInteraction}.
 */
class RpcInteractionImpl implements RpcInteraction, InteractionHandler {

  private TransactionManager _tx;
  private RpcAdapter _adapterInstance;
  private Log _log;

  RpcInteractionImpl(TransactionManager txm, Log log, RpcAdapter adapterInstance)
          throws ServiceProviderException {
    
    _tx = txm;
    _adapterInstance = adapterInstance;
    _log = log;
  }

  
  /**
   * Implementation of {@link RpcInteraction#invoke(Request, long)}:
   * converts the given request into PXE's native format, sends it on the
   * PXE message bus, and then blocks for a response which when received
   * is converted into a {@link Response} object from PXE's native
   * representation.
   * @see RpcInteraction#invoke(com.fs.pxe.xcontrib.sp.rpc.Request, long)
   */
  public Response invoke(Request request, long timeout) throws IOException, ServiceProviderException, IOException {

    if (_log.isDebugEnabled())
      _log.debug("handleNativeRequest(request=" + request +")");

    ServiceContext svcContext = _adapterInstance.getService(request);
    ServicePort port = _adapterInstance.getPort(request);
    if(port == null)
      throw new ServiceProviderException("No port found for the given request; check the pxe-system.xml or your request parameters.");
    
    // Start JTA transaction !
    try {
      _tx.begin();
    } catch (Exception e) {
      String msg = "Internal server error; unable to begin transaction!";
      _log.error(msg, e);
      throw new ServiceProviderException(msg);
    }
    
    // determine if operation has a response, i.e. is two-way
    Operation op = port.getPortType().getOperation(request.getOperation(), null, null);
    if(op == null)
      throw new ServiceProviderException("No such operation '" + request.getOperation() + "'");
    boolean oneWay = op.getOutput() == null;
    
    boolean success = false;
    MessageExchange me = null;
    // this callback will be for two-way operations only
    ResponseCallback callback = new ResponseCallback();

    try {
      Message pxemsg;
      try {
        me = svcContext.createMessageExchange(port, null, null, request.getOperation(),
                _adapterInstance.getInstanceCorrelationId());
        pxemsg = me.createInputMessage();
      } catch (NoSuchOperationException e) {
        // Should not really happen.
        String msg = "Internal error.";
        _log.error(msg, e);
        throw new ServiceProviderException(msg);
      } catch (MessageExchangeException e) {
        String msg = "Server error.";
        _log.error(msg,e);
        throw new ServiceProviderException(msg);
      }

      // Need to map all the parts provided in the request
      // to a PXE message.
      for(Iterator iter = pxemsg.getDescription().getParts().keySet().iterator(); iter.hasNext(); ){
        try{
          String partName = (String)iter.next();
        	String value = request.getPartData(partName);
          if(value == null)
            throw new ServiceProviderException("Missing expected part '" + partName + "' in the request.");
        	pxemsg.setPart(partName, DOMUtils.stringToDOM(value));
        } catch (MessageFormatException iae) {
          // Occurs when the message does not contain the required parts, or contains too many parts.
          String msg = "Invalid request: " + iae.getMessage();
          _log.error(msg,iae); 
          throw new ServiceProviderException(msg);
        } catch(SAXException e){
        	String msg = "Bad request data.";
          _log.error(msg, e);
          throw new ServiceProviderException(msg);
        }
      }

      try{
        pxemsg.checkValid();
      }catch(MessageFormatException e){
        String msg = "Invalid request msg.";
        _log.error(msg, e);
        throw new ServiceProviderException(msg, e);
      }

      try {
        me.input(pxemsg);
      } catch (Exception ex) {
        String msg = "Message Exchange Error";
        _log.error(msg,ex);
        throw new ServiceProviderException(msg);
      }

      // if we're here, everything is good :)
      success = true;
    } finally {
      if (success) {
        try {
          assert me != null;
          if(!oneWay)
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
          throw new ServiceProviderException(msg);
        }
      } else {
        try {
          _tx.rollback();
        } catch (Exception ex) {
          String msg = "Transaction rollback failed.";
          _log.error(msg ,ex);
          throw new ServiceProviderException(msg);
        }
      }
    }
    // If one way, just send back a null response.
    if (oneWay) {
      return null;
    }

    // this will block until timeout expires 
    // or until response is received
    return callback.getResponse(timeout);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.InteractionHandler#close()
   */
  public void close() {
    // nothing to do really..
  }

}
