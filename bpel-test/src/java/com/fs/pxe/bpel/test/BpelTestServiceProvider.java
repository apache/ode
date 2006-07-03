/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.test;

import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.DOMUtils;
import org.w3c.dom.Element;

import javax.transaction.TransactionManager;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service provider for representing BPEL "partners" in the test harness.
 * This provider basically shuttles events between the PXE domain and the JUnit
 * test case, {@link BpelUnitTest}.
 * Each service hosted in this provider represents one BPEL partner link.
 * The service's in-ports are mapped to the "partner role" (if any), while
 * the service's out-ports are mapped to the BPEL "my role".
 */
public class BpelTestServiceProvider implements ProtocolAdapter {
  
  private ServiceProviderContext _context;
  private IInvokerCallback _callback;
  private Map<String, ServiceContext> _svcs = new HashMap<String, ServiceContext>();

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#isRunning()
   */
  public boolean isRunning() throws ServiceProviderException {
    return true;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#activateService(com.fs.pxe.sfwk.spi.ServiceContext)
   */
  public void activateService(ServiceContext service)
                       throws ServiceProviderException {
    _svcs.put(service.getServiceName(), service);
    _callback = null;
   }

  public InteractionHandler createInteractionHandler(Class interactionClass) throws ServiceProviderException {
    return new InteractionHandlerImpl();
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#deactivateService(com.fs.pxe.sfwk.spi.ServiceContext)
   */
  public void deactivateService(ServiceContext service)
                         throws ServiceProviderException {
    _callback = null;
    _svcs.remove(service.getServiceName());
  }

  public void deployService(ServiceConfig service)
                     throws ServiceProviderException {
  }

  public void initialize(ServiceProviderContext context)
                  throws ServiceProviderException {
    _context = context;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#start()
   */
  public void start()
             throws ServiceProviderException {
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#stop()
   */
  public void stop()
            throws ServiceProviderException {
  }

  public void undeployService(ServiceConfig service)
          throws ServiceProviderException {
  }

  /**
   * Handle message exchange events.
   */
  private void onMessageExchangeEvent(MessageExchangeEvent messageExchangeEvent)
          throws ServiceProviderException {
    String cid;
    switch (messageExchangeEvent.getEventType()) {
      // Handle requests of our in-ports (i.e. requests on the partner).
      case MessageExchangeEvent.IN_RCVD_EVENT:
        InputRcvdMessageExchangeEvent evt = (InputRcvdMessageExchangeEvent)messageExchangeEvent;
        MessageExchange mex = evt.getMessageExchange();
        
        try {
          IInvokerCallback.Response cresponse
              = _callback.invokeResponse(messageExchangeEvent.getTargetService().getServiceName(), mex.getName());
          if (cresponse.faultName != null) {
            Message response = mex.createOutfaultMessage(cresponse.faultName);
            response.setMessage(DOMUtils.stringToDOM(cresponse.data));
            mex.outfault(cresponse.faultName, response);
          } else {
            Message response = mex.createOutputMessage();
            response.setMessage(DOMUtils.stringToDOM(cresponse.data));
            mex.output(response);
          }
        } catch (Exception e) {
        }        
        
        break;

        // Handle events on our out-ports (i.e. responses from the BPEL engine)
      case MessageExchangeEvent.OUT_FAULT_EVENT:
        String id = messageExchangeEvent.getMessageExchange().getCorrelationId();
        Element fault = ((OutFaultRcvdMessageExchangeEvent)messageExchangeEvent).getOutFaultMessage().getMessage();
        try {
					_callback.requestResponse(id, DOMUtils.domToString(fault));
				} catch (RemoteException e1) {
				}
        break;
        
      case MessageExchangeEvent.OUT_RCVD_EVENT:
        cid = messageExchangeEvent.getMessageExchange().getCorrelationId();
        Element msg = ((OutputRcvdMessageExchangeEvent)messageExchangeEvent).getOutputMessage().getMessage();
        try {
					_callback.requestResponse(cid, DOMUtils.domToString(msg));
				} catch (RemoteException e2) {
				}
        
        break;

      case MessageExchangeEvent.FAILURE:
        cid = messageExchangeEvent.getMessageExchange().getCorrelationId();
        try {
          _callback.requestFailed(cid,"FAILURE");
        } catch (Exception e2) {
        }
    }
  }

  public class InteractionHandlerImpl implements IBpelInvoker, InteractionHandler {
    TransactionManager _tx;

    InteractionHandlerImpl() {
      _tx = _context.getTransactionManager();
    }

    public void invokeBPEL(IInvokerCallback callback, String svc, String id, String operation, String request) throws Exception {
      boolean success = false;
      if(_callback == null && !"initial".equals(id))
        throw new IllegalStateException("First call must have id with 'initial'");
      if(_callback != null && "initial".equals(id))
        throw new IllegalStateException("'initial' call already received");
      if(_callback == null)
        _callback = callback;
      
      MessageExchange me;
      _tx.begin();

      try {
        ServiceContext service = _svcs.get(svc);
        // We only have one out-port, the one connecting us to the BPEL process.
        ServicePort port = service.getImports()[0];

        try {
          me = service.createMessageExchange(port, null, null, operation, id);
        } catch (NoSuchOperationException e1) {
          throw new ServiceProviderException(e1);
        }

        Message msg = me.createInputMessage();
        msg.setMessage(DOMUtils.stringToDOM(request));
        me.input(msg);
        success = true;
      } finally {
        try {
          if (success) {
            _tx.commit();
          } else {
            _tx.rollback();
          }
        } catch (Exception e) {
          throw new ServiceProviderException("tx err", e);
        }
      }

    }

    public void close() {}
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#onServiceEvent(com.fs.pxe.sfwk.spi.ServiceEvent)
   */
  public void onServiceEvent(ServiceEvent serviceEvent) throws ServiceProviderException, MessageExchangeException {
    if (serviceEvent instanceof MessageExchangeEvent)
      onMessageExchangeEvent((MessageExchangeEvent) serviceEvent);
    
  }

	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
	 */
	public String getProviderURI() {
		return _context.getProviderURI();
	}
}
