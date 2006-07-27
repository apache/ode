package org.apache.ode.ra.transports.rmi;

import org.apache.ode.ra.transports.OdeTransport;
import org.apache.ode.ra.transports.OdeTransportPipe;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * A very simple RMI-based communication transport.
 */
public class RMITransport implements OdeTransport {

  public OdeTransportPipe createPipe(String url, Properties properties) throws RemoteException {
    OdeRemote remoteServer;
    try {
      remoteServer = (OdeRemote) Naming.lookup(url);
    } catch (MalformedURLException e) {
      throw new RemoteException("Invalid URL: "  + url, e);
    } catch (NotBoundException e) {
      throw new RemoteException("Unable to connect to: " + url, e);
    } catch (ClassCastException cce) {
      throw new RemoteException("Protocol error: unexpected remote object type!", cce);
    }

    return remoteServer.newPipe();
  }

}
