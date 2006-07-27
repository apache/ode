/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OReply;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.FaultData;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class REPLY extends ACTIVITY {
    private static final long serialVersionUID = 3040651951885161304L;
private static final Log __log = LogFactory.getLog(REPLY.class);

  REPLY(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
  }

  public void self() {
    final OReply oreply = (OReply)_self.o;

    if (__log.isDebugEnabled()) {
      __log.debug("<reply>  partnerLink=" + oreply.partnerLink + ", operation=" + oreply.operation);
    }
    FaultData fault = null;

    // TODO: Check for fault without message.

    try {
      Node msg = getBpelRuntimeContext()
                   .fetchVariableData(_scopeFrame.resolve(oreply.variable), false);

      assert msg instanceof Element;

      for (Iterator i = oreply.initCorrelations.iterator(); i.hasNext(); ) {
        OScope.CorrelationSet cset = (OScope.CorrelationSet) i.next();
        initializeCorrelation(_scopeFrame.resolve(cset),
                _scopeFrame.resolve(oreply.variable));
      }

      //		send reply 
      getBpelRuntimeContext()
        .reply(_scopeFrame.resolve(oreply.partnerLink), oreply.operation.getName(),
                oreply.messageExchangeId,
                (Element)msg,
               (oreply.fault != null)
               ? oreply.fault.getName()
               : null);
    } catch (FaultException e) {
      fault = createFault(e.getQName(), oreply);
    }

    _self.parent.completed(fault, CompensationHandler.emptySet());
  }
}
