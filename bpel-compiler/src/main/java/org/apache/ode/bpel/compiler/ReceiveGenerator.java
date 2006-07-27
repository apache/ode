/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.ReceiveActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OEmpty;
import org.apache.ode.bpel.o.OPickReceive;

class ReceiveGenerator extends PickReceiveGenerator {

  public void compile(OActivity output, Activity src) {
    OPickReceive opick = (OPickReceive) output;
    ReceiveActivity rcvDef = (ReceiveActivity) src;

    opick.createInstanceFlag = rcvDef.getCreateInstance();
    OPickReceive.OnMessage onMessage = compileOnMessage(
            rcvDef.getVariable(),
            rcvDef.getPartnerLink(),
            rcvDef.getOperation(),
            rcvDef.getMessageExchangeId(),
            rcvDef.getPortType(),
            rcvDef.getCreateInstance(),
            rcvDef.getCorrelations());

    onMessage.activity = new OEmpty(_context.getOProcess());
    opick.onMessages.add(onMessage);
  }

}
