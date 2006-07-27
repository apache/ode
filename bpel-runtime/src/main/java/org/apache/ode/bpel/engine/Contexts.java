package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.Scheduler;

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
