/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.test;


/**
 * Invocation API for the {@link com.fs.pxe.bpel.runtime.harness.MockPartnerServiceProvider}.
 * Allows the test case to tell the virtual "partner" to invoke the BPEL process.
 */
public interface IBpelInvoker  {

  /**
   * Make a request from the "partner" to the BPEL process.
   * @param id TestCase's identifier for the request (correlation id)
   * @param operation name of the operation
   * @param request message root element
   */
  public void invokeBPEL(IInvokerCallback callback, String svc, String id, String operation, String request) throws Exception;
}
