package com.fs.pxe.sfwk.impl.mock.pconn;

import com.fs.pxe.sfwk.spi.*;

import java.io.Serializable;

/**
 * A protocol connector that can be configured to deliver message exchange events
 * to an arbitrary Java class. This protocol adpater is intended for use in test
 * cases (mock applications).  
 */
public class MockProtocolConnector implements ServiceProvider {

  private static ServiceEventHandler __serviceEventHandler;

  private ServiceProviderContext _context;


  public boolean isRunning() throws ServiceProviderException {
    return true;
  }


  public void activateService(ServiceContext service) throws ServiceProviderException {
    // Do nothing.
  }

  public void onServiceEvent(ServiceEvent serviceEvent) throws ServiceProviderException, MessageExchangeException {
    if (serviceEvent instanceof MessageExchangeEvent)
      __serviceEventHandler.onMessageExchange(serviceEvent.getTargetService(), (MessageExchangeEvent) serviceEvent);
    else if (serviceEvent instanceof ScheduledWorkEvent)
      __serviceEventHandler.onScheduledWorkEvent(serviceEvent.getTargetService(), ((ScheduledWorkEvent)serviceEvent).getPayload());
  }


  public InteractionHandler createInteractionHandler(Class interactionClass) throws ServiceProviderException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void deactivateService(ServiceContext service) throws ServiceProviderException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void deployService(ServiceConfig service) throws ServiceProviderException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void initialize(ServiceProviderContext context) throws ServiceProviderException {
    _context = context;
  }

  public void start() throws ServiceProviderException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void stop() throws ServiceProviderException {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void undeployService(ServiceConfig service) throws ServiceProviderException {
    //To change body of implemented methods use File | Settings | File Templates.
  }


  public static void setMessageExchangeListenerFactory(ServiceEventHandler serviceEventHandler) {
    __serviceEventHandler = serviceEventHandler;
  }

  public interface ServiceEventHandler {

    void onMessageExchange(ServiceContext service, MessageExchangeEvent messageExchangeEvent)
            throws ServiceProviderException, MessageExchangeException;

    void onScheduledWorkEvent(ServiceContext service, Serializable payload)
            throws ServiceProviderException;
  }

	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
	 */
	public String getProviderURI() {
		return _context.getProviderURI();
	}
}
