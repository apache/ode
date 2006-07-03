/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.o.*;

/**
 * Factory for creating activity template objects.
 */
public class ActivityTemplateFactory {
  
  public ACTIVITY createInstance(OActivity type, ActivityInfo ai, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    if (type instanceof OThrow) return new THROW(ai, scopeFrame, linkFrame);
    if (type instanceof OEmpty) return new EMPTY(ai, scopeFrame, linkFrame);
    if (type instanceof OAssign) return new ASSIGN(ai, scopeFrame, linkFrame);
    if (type instanceof OCompensate) return new COMPENSATE(ai, scopeFrame, linkFrame);
    if (type instanceof OFlow) return new FLOW(ai, scopeFrame, linkFrame);
    if (type instanceof OInvoke) return new INVOKE(ai, scopeFrame, linkFrame);
    if (type instanceof OPickReceive) return new PICK(ai, scopeFrame, linkFrame);
    if (type instanceof OReply) return new REPLY(ai, scopeFrame, linkFrame);
    if (type instanceof ORethrow) return new RETHROW(ai, scopeFrame, linkFrame);
    if (type instanceof OScope) return new SCOPEACT(ai, scopeFrame, linkFrame);
    if (type instanceof OSequence) return new SEQUENCE(ai, scopeFrame, linkFrame);
    if (type instanceof OSwitch) return new SWITCH(ai, scopeFrame, linkFrame);
    if (type instanceof OTerminate) return new TERMINATE(ai, scopeFrame, linkFrame);
    if (type instanceof OWait) return new WAIT(ai, scopeFrame, linkFrame);
    if (type instanceof OWhile) return new WHILE(ai, scopeFrame, linkFrame);
    if (type instanceof OForEach) return new FOREACH(ai, scopeFrame, linkFrame);

    throw new IllegalArgumentException("Unknown type: " + type);
  }

}
