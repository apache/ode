package com.fs.pxe.bpel.engine;

import com.fs.pxe.bpel.dao.BpelDAOConnectionFactory;
import com.fs.pxe.bpel.iapi.EndpointReferenceContext;
import com.fs.pxe.bpel.iapi.MessageExchangeContext;
import com.fs.pxe.bpel.iapi.Scheduler;

/**
 * Aggregation of all the contexts provided to the BPEL engine
 * by the integration layer.
 */
class Contexts {

  MessageExchangeContext mexContext;
  Scheduler scheduler;
  EndpointReferenceContext eprContext;
  BpelDAOConnectionFactory dao;
}
