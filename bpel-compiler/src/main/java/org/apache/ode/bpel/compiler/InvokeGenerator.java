/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Constants;
import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bom.api.InvokeActivity;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OInvoke;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.utils.msg.MessageBundle;

import java.util.Collection;
import java.util.List;


/**
 * Generates code for <code>&lt;invoke&gt;</code> activities. 
 */
class InvokeGenerator extends DefaultActivityGenerator {

  private static final CommonCompilationMessages __cmsgs = MessageBundle.getMessages(CommonCompilationMessages.class);
  private static final InvokeGeneratorMessages __imsgs = MessageBundle.getMessages(InvokeGeneratorMessages.class);

  public OActivity newInstance(Activity src) {
    return new OInvoke(_context.getOProcess());
  }

  public void compile(OActivity output, Activity srcx)  {
    InvokeActivity src = (InvokeActivity)srcx;
    OInvoke oinvoke = (OInvoke) output;

    oinvoke.partnerLink = _context.resolvePartnerLink(src.getPartnerLink());
    oinvoke.operation = _context.resolvePartnerRoleOperation(oinvoke.partnerLink, src.getOperation());
    assert oinvoke.operation.getInput() != null; // ensured by resolvePartnerRoleOperation
    assert oinvoke.operation.getInput().getMessage() != null; // ensured by resolvePartnerRoleOperation
    //TODO: Add portType checking if specified by user
//    if (portType != null && !portType.equals(onMessage.partnerLink.myRolePortType.getQName()))
//      throw new CompilationException(CMSGSG.errPortTypeMismatch(portType, onMessage.partnerLink.myRolePortType.getQName()));

    if (oinvoke.operation.getInput() != null && oinvoke.operation.getInput().getMessage() != null) {
      if (src.getInputVar() == null)
        throw new CompilationException(__imsgs.errInvokeNoInputMessageForInputOp(oinvoke.operation.getName()));
      oinvoke.inputVar = _context.resolveMessageVariable(src.getInputVar(), oinvoke.operation.getInput().getMessage().getQName());
    }
    
    if (oinvoke.operation.getOutput() != null && oinvoke.operation.getOutput().getMessage() != null) {
      if (src.getOutputVar() == null)
        throw new CompilationException(__imsgs.errInvokeNoOutputMessageForOutputOp(oinvoke.operation.getName()));
      oinvoke.outputVar = _context.resolveMessageVariable(src.getOutputVar(), oinvoke.operation.getOutput().getMessage().getQName());
    }

    if (oinvoke.inputVar != null)
      doCorrelations(src.getCorrelations(Correlation.CORRPATTERN_OUT), oinvoke.inputVar, oinvoke.assertCorrelationsInput, oinvoke.initCorrelationsInput);

    if (oinvoke.outputVar != null)
      doCorrelations(src.getCorrelations(Correlation.CORRPATTERN_IN), oinvoke.outputVar, oinvoke.assertCorrelationsOutput, oinvoke.initCorrelationsOutput);

    if (!oinvoke.getOwner().version.equals(Constants.NS_BPEL4WS_2003_03)) {
      if (!oinvoke.partnerLink.initializePartnerRole && !_context.isPartnerLinkAssigned(oinvoke.partnerLink.getName())) {
        throw new CompilationException(__cmsgs.errUninitializedPartnerLinkInInvoke(oinvoke.partnerLink.getName()));
      }
    }

  }

  private void doCorrelations(List<Correlation> correlations, OScope.Variable var, Collection<OScope.CorrelationSet> assertCorrelations, Collection<OScope.CorrelationSet> initCorrelations) {
    for (Correlation correlation : correlations) {
      OScope.CorrelationSet cset = _context.resolveCorrelationSet(correlation.getCorrelationSet());

      switch (correlation.getInitiate()) {
        case Correlation.INITIATE_NO:
          assertCorrelations.add(cset);
          break;
        case Correlation.INITIATE_YES:
          initCorrelations.add(cset);
          break;
        case Correlation.INITIATE_RENDEZVOUS:
          // TODO: fixe errror
          throw new UnsupportedOperationException();
      }

      for (OProcess.OProperty property : cset.properties) {
        // Force resolution of alias, to make sure that we have one for this variable-property pair.
        try {
          _context.resolvePropertyAlias(var, property.name);
        } catch (CompilationException ce) {
          if (ce.getCompilationMessage().source == null) {
            ce.getCompilationMessage().source = correlation;
          }
          throw ce;
        }
        //onMessage.
      }
    }
  }
}
