/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra.transports;

import java.rmi.RemoteException;
import java.util.Properties;


/**
 * Interface implemented communication transports for the PXE JCA adapter.
 */
public interface PxeTransport {
  
  /**
   * Create a new communication pipe.
   * @param url connection URL
   * @param properties transport-specific communication properties
   * @return new communication pipe
   * @throws RemoteException in case of communication failure
   */
  public PxeTransportPipe createPipe(String url, Properties properties)
          throws RemoteException ;

}
