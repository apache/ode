/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import com.fs.jacob.SynchChannel;
import org.apache.ode.bpel.evt.ProcessInstanceStartedEvent;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.ParentScopeML;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;

import java.util.Set;

public class PROCESS extends BpelAbstraction {
	private static final long serialVersionUID = 1L;
	private OProcess _process;

  public PROCESS(OProcess process) {
    _process = process;
  }

  public void self() {
    BpelRuntimeContext ntive = getBpelRuntimeContext();
    Long scopeInstanceId = ntive.createScopeInstance(null, _process.procesScope);

    ProcessInstanceStartedEvent evt = new ProcessInstanceStartedEvent();
    evt.setRootScopeId(scopeInstanceId);
    evt.setScopeDeclarationId(_process.procesScope.getId());
    ntive.sendEvent(evt);

    ActivityInfo child = new ActivityInfo(genMonotonic(),
            _process.procesScope,
            newChannel(TerminationChannel.class), newChannel(ParentScopeChannel.class));
    ScopeFrame processFrame = new ScopeFrame(_process.procesScope, scopeInstanceId, null, null);
    instance(new SCOPE(child, processFrame, new LinkFrame(null)));

    object(new ParentScopeML(child.parent) {
    private static final long serialVersionUID = -8564969578471906493L;

    public void compensate(OScope scope, SynchChannel ret) {
        assert false;
      }

      public void completed(FaultData fault, Set<CompensationHandler> compensations) {
        BpelRuntimeContext nativeAPI = (BpelRuntimeContext)getExtension(BpelRuntimeContext.class);
        if (fault == null) {
          nativeAPI.completedOk();
        } else {
          nativeAPI.completedFault(fault);
        }
      }});
  }

}
