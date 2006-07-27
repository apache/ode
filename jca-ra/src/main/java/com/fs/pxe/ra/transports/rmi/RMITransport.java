package com.fs.pxe.ra.transports.rmi;

import com.fs.pxe.ra.transports.PxeTransport;
import com.fs.pxe.ra.transports.PxeTransportPipe;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * A very simple RMI-based communication transport.
 */
public class RMITransport implements PxeTransport {

  public PxeTransportPipe createPipe(String url, Properties properties) throws RemoteException {
    PxeRemote remoteServer;
    try {
      remoteServer = (PxeRemote) Naming.lookup(url);
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
