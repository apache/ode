package org.apache.ode.bpel.connector;

import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.jca.server.rmi.RmiTransportServerImpl;

public class BpelServerConnector {

  private BpelServer _server;
  private RmiTransportServerImpl _transport;
  
  public BpelServerConnector() {
    _transport = new RmiTransportServerImpl();
    _transport.setId("ode/BpelEngine");
  }
  
  public void setBpelServer(BpelServer server) {
    _server = server;
  }
  
  public void start() throws Exception {
    if (_server == null)
      throw new IllegalStateException("Server not set!");
    
    _transport.setConnectionProvider(new ConnectionProviderImpl(_server));
    _transport.start();

  }
  
  public void shutdown() throws Exception {
    _transport.stop();
  }

  public void setPort(int connectorPort) {
    _transport.setPort(connectorPort);
  }

  public void setId(String connectorName) {
    _transport.setId(connectorName);
  }
  
}
