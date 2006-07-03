/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.xcontrib.sp.rpc;

import java.io.IOException;
import com.fs.pxe.sfwk.spi.ServiceProviderException;


/**
 * <p>
 * A interaction providing a blocking RPC mechanism for invoking
 * synchronous web service operations. This interface is used to
 * communicate with the RPC Protocol Adapter via PXE's universal
 * JCA connector.
 * </p>
 *
 */
public interface RpcInteraction {

  /**
   * Do a synchronous invoke on the {@link RpcAdapter}. This method
   * will block until a response is available or until the specified
   * timeout expires.
   *
   * @param request the request
   * @param timeout if two-way call, maximum time in ms to block
   *
   * @return response or <code>null</code>, if one-way service
   *
   * @throws ServiceProviderException in case of serious provider error
   */
  public Response invoke(Request request, long timeout)
                         throws ServiceProviderException, IOException;

}
