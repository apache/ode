
/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

/*
 * Created on Mar 16, 2004
 *
 */
package com.fs.pxe.sfwk.impl.mock.padapt;

import com.fs.pxe.sfwk.spi.*;
import com.fs.utils.DOMUtils;

import javax.transaction.TransactionManager;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import java.util.HashMap;
import java.util.Map;


/**
 * Service provider for test harness: receive/reply messages.
 */
public class MockProtocolAdapter implements ProtocolAdapter {
  private Map<String, String> _responses = new HashMap<String, String>();
  private ServiceProviderContext _context;
  private Map<String, ServiceContext> _service = new HashMap<String, ServiceContext>();
  private static Runner __runner;

  public static void setRunner(Runner runner) {
    __runner = runner;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#isRunning()
   */
  public boolean isRunning()
                    throws ServiceProviderException {
    return true;
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#activateService(com.fs.pxe.sfwk.spi.ServiceContext)
   */
  public void activateService(ServiceContext service)
                       throws ServiceProviderException {
    _service.put(service.getServiceName(),service);
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#createInteractionHandler(Class)
   * @param interactionClass
   */
  public InteractionHandler createInteractionHandler(Class interactionClass)
                                              throws ServiceProviderException {
    return new InteractionHandlerImpl();
  }


  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#deactivateService(com.fs.pxe.sfwk.spi.ServiceContext)
   */
  public void deactivateService(ServiceContext service)
                         throws ServiceProviderException {
  }

  public void deployService(ServiceConfig service)
                     throws ServiceProviderException {
  }

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#initialize(com.fs.pxe.sfwk.spi.ServiceProviderContext)
   */
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

  public class InteractionHandlerImpl implements MockProtocolAdapterInteraction, InteractionHandler {
    private ServiceContext _svc;
    private TransactionManager _tx;

    InteractionHandlerImpl() {
      _tx = _context.getTransactionManager();
    }

    public void setTargetService(String serviceName) {
      _svc = _service.get(serviceName);
    }

    public void close() {
    }

    /**
     */
    public String sendMessage(String portName, String operation, String msgE, long timeout)
                        throws Exception {
      boolean success = false;
      MessageExchange me;
      _tx.begin();
      ServicePort port = (portName == null)
        ? _svc.getImports()[0]
        : _svc.getImport(portName);
        
      try {
        try {
          me = _svc.createMessageExchange(port,null,null, operation);
        } catch (NoSuchOperationException e1) {
          throw new ServiceProviderException(e1);
        }

        Message msg = me.createInputMessage();
        msg.setMessage(DOMUtils.stringToDOM(msgE));
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

      PortType portType = port.getPortType();
      Operation op = portType.getOperation(operation, null, null);

      return (op.getOutput() == null)
             ? null
             : getResponse(me, timeout);
    }

    private String getResponse(MessageExchange me, long timeout)
                         throws Exception {
      String reply;
      // this should block until 'timeout'
      if(timeout < 0)
        timeout = 0;
      long ctime = System.currentTimeMillis();
      long etime = ctime + timeout;
      synchronized (_responses) {
        while (!_responses.containsKey(me.getInstanceId()) && (timeout == 0 || (ctime = System.currentTimeMillis()) < etime))
          _responses.wait(etime-ctime);
        reply = _responses.remove(me.getInstanceId());
      }


      if (reply == null) {
        return null;
      }

      return reply;
    }

    public void run() {
      __runner.run(_svc);
    }

		public String sendMessage(String op, String msg) throws MessageExchangeException, ServiceProviderException, Exception {
			return sendMessage(null, op, msg, 60 * 1000);
		}
  }

  public void onServiceEvent(ServiceEvent serviceEvent) throws ServiceProviderException, MessageExchangeException {
    if (serviceEvent instanceof MessageExchangeEvent) {
      MessageExchangeEvent msgExEvent = (MessageExchangeEvent) serviceEvent;
      Message msg = null;
      switch (msgExEvent.getEventType()) {
        case MessageExchangeEvent.OUT_FAULT_EVENT:
          msg = ((OutFaultRcvdMessageExchangeEvent)msgExEvent).getOutFaultMessage();
          synchronized (_responses) {
            _responses.put(msgExEvent.getInstanceId(), DOMUtils.domToString(msg.getMessage()));
            _responses.notifyAll();
          }
          break;

        case MessageExchangeEvent.OUT_RCVD_EVENT:
          msg = ((OutputRcvdMessageExchangeEvent)msgExEvent).getOutputMessage();
          synchronized (_responses) {
            _responses.put(msgExEvent.getInstanceId(), DOMUtils.domToString(msg.getMessage()));
            _responses.notifyAll();
          }
          break;
        case MessageExchangeEvent.FAILURE:
          synchronized (_responses) {
            _responses.put(msgExEvent.getInstanceId(), "FAILURE");
            _responses.notifyAll();
          }
          break;

        default:
          throw new ServiceProviderException("Unknown Event!");
      }

    }
  }

  public interface Runner {
    public void run(ServiceContext service);
  }

	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
	 */
	public String getProviderURI() {
		return _context.getProviderURI();
	}

}
