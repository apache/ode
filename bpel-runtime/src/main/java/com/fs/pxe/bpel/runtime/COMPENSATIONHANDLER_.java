/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.jacob.SynchChannel;
import com.fs.pxe.bpel.evt.CompensationHandlerRegistered;
import com.fs.pxe.bpel.evt.ScopeEvent;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.channels.*;

import java.util.Iterator;
import java.util.Set;

/**
 * A scope that has completed succesfully, and may possibly have a compensation handler.
 */
class COMPENSATIONHANDLER_ extends BpelAbstraction {
	private static final long serialVersionUID = 1L;
	private CompensationHandler _self;
  private Set<CompensationHandler> _completedChildren;

  public COMPENSATIONHANDLER_(CompensationHandler self, Set<CompensationHandler> visibleCompensations) {
    _self = self;
    _completedChildren = visibleCompensations;
  }

  public void self() {
    sendEvent(new CompensationHandlerRegistered());
    object(new CompensationML(_self.compChannel) {
    private static final long serialVersionUID = -477602498730810094L;

    public void forget() {
         // Tell all our completed children to forget.
         for (Iterator<CompensationHandler> i = _completedChildren.iterator(); i.hasNext(); )
           i.next().compChannel.forget();
       }

       public void compensate(final SynchChannel ret) {
         // Only scopes with compensation handlers can be compensated.
         assert _self.compensated.oscope.compensationHandler != null;

         ActivityInfo ai = new ActivityInfo(genMonotonic(),
                 _self.compensated.oscope.compensationHandler,
                 newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));


         ScopeFrame compHandlerScopeFrame = new ScopeFrame(
                 _self.compensated.oscope.compensationHandler,
                 getBpelRuntimeContext().createScopeInstance(_self.compensated.scopeInstanceId, _self.compensated.oscope.compensationHandler),
                 _self.compensated,
                 _completedChildren);

         // Create the compensation handler scope.
         instance(new SCOPE(ai,compHandlerScopeFrame, new LinkFrame(null)));

         object(new ParentScopeML(ai.parent) {
        private static final long serialVersionUID = 8044120498580711546L;

        public void compensate(OScope scope, SynchChannel ret) {
             throw new AssertionError("Unexpected.");
           }

           public void completed(FaultData faultData, Set<CompensationHandler> compensations) {
             // TODO: log faults.

             // Compensations registered in a compensation handler are unreachable.
             for (Iterator<CompensationHandler> i = compensations.iterator();i.hasNext(); ) {
               i.next().compChannel.forget();
             }

             // Notify synchronized waiter that we are done.
             ret.ret();
           }
         });
       }
     });
  }

  private void sendEvent(ScopeEvent event) {
    _self.compensated.fillEventInfo(event);
    getBpelRuntimeContext().sendEvent(event);
  }

  public String toString() {
   return new StringBuffer(getClassName())
		.append(":")
    .append(_self.compensated)
    .append("(...)")
		.toString();
  }
}
