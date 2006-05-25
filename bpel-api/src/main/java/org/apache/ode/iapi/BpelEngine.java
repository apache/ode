package org.apache.ode.iapi;

import javax.xml.namespace.QName;

/**
 * Interface exposing the BPEL "engine". Basically, this interface facilitates
 * dropping off events for processing by the engine. It is expected that
 * <em>all</em> of the methods of this interface are called only from a 
 * "transactional context" (i.e. a transaction is associated with the thread
 * invoking the method). The exact nature of the transactional context is not
 * specified, however it must be the same context that is used by the BPEL
 * DAO layer implementation. If a method invoked from this interface throws 
 * an exception of any kind, then the current transaction <em>must</em> be 
 * rolled back. 
 */
public interface BpelEngine {
  /**
   * Check whether the given endpoint reference corresponds to a "my role" 
   * partner link in some process deployed in the engine. This method is
   * provided so that the integration layer can route to multiple BPEL engines.
   * @param epr target endpoint reference 
   * @return <code>true</code> if the EPR corresponds to a my-role of an
   *         active proces, <code>false</code> otherwise.
   */
  boolean isMyRoleEndpoint(EndpointReference epr)
    throws BpelEngineException; 


  /** 
   *  Create a "my role" message exchange for invoking a BPEL process. 
   *  
   *  @param epr endpoint reference 
   *  @param operation name of the operation
   *  @return {@link MyRoleMessageExchange} the newly created message
   *          exchange 
   */
  MyRoleMessageExchange createMessageExchange(
      EndpointReference epr, 
      String operation)
    throws BpelEngineException; 



  /**
   * Retrieve a message identified by the given identifer.
   * @param mexId message exhcange identifier
   * @return associated message exchange 
   */
  MessageExchange getMessageExchange(String mexId);


}
