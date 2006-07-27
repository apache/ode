/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra.transports;

import java.rmi.RemoteException;
import java.util.Properties;


/**
 * Interface implemented communication transports for the ODE JCA adapter.
 */
public interface OdeTransport {
  
  /**
   * Create a new communication pipe.
   * @param url connection URL
   * @param properties transport-specific communication properties
   * @return new communication pipe
   * @throws RemoteException in case of communication failure
   */
  public OdeTransportPipe createPipe(String url, Properties properties)
          throws RemoteException ;

}
