package org.apache.ode.bpel.connector;

import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.jca.server.ConnectionProvider;

/**
 * Implementation of the {@link org.apache.ode.jca.server.ConnectionProvider}
 * interface: provides {@link org.apache.ode.bpel.pmapi.BpelManagementFacade}
 * objects. 
 */
class ConnectionProviderImpl implements ConnectionProvider {

  private BpelServer _server;
  
  ConnectionProviderImpl(BpelServer server) {
    _server = server;
  }
  
  public String [] getConnectionIntefaces() {
    return new String[] { "org.apache.ode.bpel.jca.clientapi.ProcessManagementConnection" };
  }

  public Object createConnectionObject() {
    return _server.getBpelManagementFacade();
  }

  /**
   * Does nothing.
   */
  public void destroyConnectionObject(Object cobj) {
    ((BpelManagementFacade)cobj).toString();
  }


}
