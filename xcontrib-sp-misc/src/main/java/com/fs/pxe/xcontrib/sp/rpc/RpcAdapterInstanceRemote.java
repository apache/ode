/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.xcontrib.sp.rpc;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface exposed by each native protocol adapter instance.
 * Used for correlating responses to the correct adapter instance.
 */
interface RpcAdapterInstanceRemote extends Remote {

  /**
   * Notify a particular adapter instance that a response to a request originating
   * from that instance is available. It is important to reach the correct instance in this
   * case, as the blocking invoker thread will be associated with only one instance.
   * @param messageExchangeId message exchange identifier
   * @param response response
   * @throws RemoteException RMI error
   */
  void onResponseReceived(String messageExchangeId, Response response)
          throws IllegalArgumentException, RemoteException ;
}
