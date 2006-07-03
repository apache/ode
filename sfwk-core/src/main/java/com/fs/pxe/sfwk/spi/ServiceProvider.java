/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.spi;


/**
 * Primary interface implemented by all PXE Service Providers; it provides several
 * methods for controlling the activation of PXE Services and one for consuming
 * PXE Service events (i.e. message-exchange and scheduled work events).
 *
 * <p>
 * <em>NOTE: This interface must be implemented by the Service Provider
 *           implementation. </em>
 * </p>
 */
public interface ServiceProvider {

	public String getProviderURI();
	
	/**
   * Return whether the service provider is running.
   *
   * @return true if service provider is running
   * @throws ServiceProviderException
   */
  public boolean isRunning()
                    throws ServiceProviderException;

  /**
   * Activate a service; this method is called by the framework for each
   * "activated" deployment. The method MUST be idempotent, and the service
   * provider MUST NOT attempt to "remember" the activation state in
   * persistent storage; the activation state is maintained by the framework
   * and it will notify the provider of that state through this method.
   *
   * @param service service to startActivity
   *
   * @throws ServiceProviderException
   */
  public void activateService(ServiceContext service)
                       throws ServiceProviderException;


  /**
   * Deactivate a service; this method is called by the framework for each
   * "inactive" deployment. This method MUST be idempotent.
   *
   * @param service service to deactivate
   *
   * @throws ServiceProviderException
   *
   * @see #activateService
   */
  public void deactivateService(ServiceContext service)
                         throws ServiceProviderException;


  /**
   * Create an {@link InteractionHandler} to handle a client ession.
   * Cient sessions allow external components to communicate with a
   * Service Provider through a standard JCA mechanism. 
   *
   * @return an instance of a Service Provider-defined interface that
   *         declares the methods that are to be visible to external
   *         components (clients).
   *
   * @throws ServiceProviderException
   * @param interactionClass
   */
  public Object createInteractionHandler(Class interactionClass)
                                              throws ServiceProviderException;

  
  /**
   * Deploy a service in the provider. The side-effects of this operation are
   * permanent: once a service is deployed it stays deployed even after
   * system shutdown.
   *
   * @param service PXE Service being deployed
   * @throws ServiceProviderException in case of failure
   */
  public void deployService(ServiceConfig service)
                     throws ServiceProviderException;

  /**
   * Undeploy a previously deployed service. Once a service is undeployed, the
   * provider is free to forget about the service and any associated data.
   *
   * @throws ServiceProviderException in case of failure
   */
  public void undeployService(ServiceConfig service)
                       throws ServiceProviderException;


  /**
   * Initialize the provider. This method is called by the PXE container
   * framework during system (domain-node) startup.
   *
   * @param context service provider context
   *
   * @throws ServiceProviderException in case of failure
   */
  public void initialize(ServiceProviderContext context)
                  throws ServiceProviderException;

  /**
   * Called to start the service provider. The provider should not attempt to
   * process messages or perform any actions on behalf of its services until
   * it is started.
   *
   * @throws ServiceProviderException in case of failure
   */
  public void start()
             throws ServiceProviderException;

  /**
   * Called to stop the service provider.
   *
   * @throws ServiceProviderException in case of failure
   */
  public void stop()
            throws ServiceProviderException;

  /**
   * Method used to consume PXE Service events ({@link ServiceEvent}).
   * These events come in two flavors: <em>message-exchange events</em>, and
   * <em>scheduled work events</em>.  The former is used to communicate a change
   * in the status of message-exchange involving the target PXE Service, while the
   * latter is used to notify the provider that a work item previously scheduled by
   * the target PXE Service is now "up" for execution.
   *
   * <p>
   * This method is always invoked from a thread that has been associated with
   * a transaction context. Failure of the method (if it throws an exception)
   * will generally result in the transaction being rolled back and subsequent
   * re-delivery of the event.
   * @todo Add ServiceEventProcessingException
   * some
   * </p>
   * <p>NOTE: This method MUST be thread-safe! </p>
   *
   * @param serviceEvent PXE Service event
   * @throws ServiceProviderException in case of a service provider error
   *         MessageExchagneException todo: remove?
   */
  public void onServiceEvent(ServiceEvent serviceEvent)
          throws ServiceProviderException, MessageExchangeException;

}
