package com.fs.pxe.bpel.connector;

import com.fs.pxe.bpel.iapi.BpelServer;
import com.fs.pxe.bpel.pmapi.BpelManagementFacade;
import com.fs.pxe.jca.server.ConnectionProvider;

/**
 * Implementation of the {@link com.fs.pxe.jca.server.ConnectionProvider}
 * interface: provides {@link com.fs.pxe.bpel.pmapi.BpelManagementFacade}
 * objects. 
 */
class ConnectionProviderImpl implements ConnectionProvider {

  private BpelServer _server;
  
  ConnectionProviderImpl(BpelServer server) {
    _server = server;
  }
  
  public String [] getConnectionIntefaces() {
    return new String[] { "com.fs.pxe.bpel.jca.clientapi.ProcessManagementConnection" };
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
