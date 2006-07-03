/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.*;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OPickReceive;

import java.util.Iterator;


/**
 * Generates code for <code>&lt;pick&gt;</code> activities.
 */
class PickGenerator extends PickReceiveGenerator {

  public OActivity newInstance(Activity src) {
    return new OPickReceive(_context.getOProcess());
  }

  public void compile(OActivity output, Activity src) {
    OPickReceive opick = (OPickReceive) output;
    PickActivity pickDef = (PickActivity) src;

    opick.createInstanceFlag = pickDef.isCreateInstance();
    for (Iterator<OnMessage> i = pickDef.getOnMessages().iterator(); i.hasNext(); ) {
      OnMessage sOnMessage = i.next();
      OPickReceive.OnMessage oOnMessage = compileOnMessage(sOnMessage.getVariable(),
              sOnMessage.getPartnerLink(),
              sOnMessage.getOperation(),
              sOnMessage.getMessageExchangeId(),
              sOnMessage.getPortType(),
              pickDef.isCreateInstance(),
              sOnMessage.getCorrelations());
      oOnMessage.activity = _context.compile(sOnMessage.getActivity());
      opick.onMessages.add(oOnMessage);
    }

    try {
      for(Iterator<OnAlarm> i = pickDef.getOnAlarms().iterator(); i.hasNext(); ){
      	OnAlarm onAlarmDef = i.next();
        OPickReceive.OnAlarm oalarm = new OPickReceive.OnAlarm(_context.getOProcess());
        oalarm.activity = _context.compile(onAlarmDef.getActivity());
        if (onAlarmDef.getFor() != null && onAlarmDef.getUntil() == null) {
          oalarm.forExpr = _context.compileExpr(onAlarmDef.getFor());
        } else if (onAlarmDef.getFor() == null && onAlarmDef.getUntil() != null) {
          oalarm.untilExpr = _context.compileExpr(onAlarmDef.getUntil());
        } else {
          throw new CompilationException(__cmsgs.errForOrUntilMustBeGiven().setSource(onAlarmDef));
        }

        if (pickDef.isCreateInstance())
          throw new CompilationException(__cmsgs.errOnAlarmWithCreateInstance().setSource(onAlarmDef));

        opick.onAlarms.add(oalarm);
      }
    } catch (CompilationException ce) {
      _context.recoveredFromError(pickDef, ce);
    }
  }
}
