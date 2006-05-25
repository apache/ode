/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.iapi;

import java.io.IOException;
import java.net.URI;

import javax.xml.namespace.QName;

/**
 * Interface implemented by the BPEL server. Provides methods for
 * life-cycle management.
 */
public interface BpelServer {

  /**
   * Configure the {@see BpelEngine} with a message-exchange context. BPEL
   * engine uses this context to initiate communication with external
   * services.
   * @see MessageExchangeContext
   * @param mexContext {@link MessageExchangeContext} implementation
   */
  void setMessageExchangeContext(MessageExchangeContext mexContext) 
    throws BpelEngineException; 
  
  /**
   * Configure the {@see BpelEngine} with a scheduler.
   */
  void setScheduler(Scheduler scheduler)
    throws BpelEngineException; 
  

  /**
   * Configure the {@see BpelEngine} with an endpoint-reference (EPR) context.
   * BPEL engine uses this context to EPRs.
   * @see EndpointReferenceContext
   * @param eprContext {@link EndpointReferenceContext} implementation
   */  
  void setEndpointReferenceContext(EndpointReferenceContext eprContext)
    throws BpelEngineException; 

  
  /**
   * Set the DAO connection factory. The DAO is used by the BPEL engine
   * to persist information about active processes.
   * @param daoCF {@link BpelDAOConnectionFactory} implementation.
   */
// TODO Specify a DAO layer
//  void setDaoConnectionFactory(BpelDAOConnectionFactory daoCF)
//    throws BpelEngineException;

  
  /**
   * Initialize the BPEL engine. The various contexts needed by the
   * engine must be configured before this method is called. 
   */
  void init()
    throws BpelEngineException; 

  /**
   * Start the BPEL engine. The BPEL engine will not execute process
   * instances until it is started.
   */
  void start()
    throws BpelEngineException; 


  /**
   * Stop the BPEL engine: results in the cessation of process
   * execution.
   */
  void stop()
    throws BpelEngineException; 

            

  /**
   * Called to shutdown the BPEL egnine. 
   */
  void shutdown()
    throws BpelEngineException; 

  
  /**
   * Get the {@link BpelEngine} interface.
   * @return {@link BpelEngine} implementation
   */
  BpelEngine getEngine();
  
  QName deploy(URI deployURI) throws IOException;
  
  void undeploy(QName pid);
  
  void undeploy(URI deployURI);

  // TODO Specify a management API
//  BpelManagementFacade getBpelManagementFacade();
  
}
