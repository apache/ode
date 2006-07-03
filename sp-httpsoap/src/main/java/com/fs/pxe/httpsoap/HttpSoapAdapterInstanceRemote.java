/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.httpsoap;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface exposed by each HttpSoap protocol adapter instance.
 * Used for correlating responses to the correct adapter instance.
 */
interface HttpSoapAdapterInstanceRemote extends Remote {

  /**
   * Notify a particular HttpSoapAdapterInbound instance that a response to a request originating
   * from that instance is available. It is important to reach the correct instance in this
   * case, as the blocking HTTP thread will be associated with only one instance.
   * @param messageExchangeId message exchange identifier
   * @param response response
   * @throws RemoteException RMI error
   */
  void onResponseReceived(String messageExchangeId, HttpSoapResponse response)
          throws IllegalArgumentException,  RemoteException ;
}
