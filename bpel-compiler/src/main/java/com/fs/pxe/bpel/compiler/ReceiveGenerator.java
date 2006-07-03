/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Correlation;
import com.fs.pxe.bom.api.ReceiveActivity;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OEmpty;
import com.fs.pxe.bpel.o.OPickReceive;

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
