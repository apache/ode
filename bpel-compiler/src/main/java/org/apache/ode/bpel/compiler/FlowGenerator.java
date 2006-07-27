/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.CompositeActivity;
import org.apache.ode.bom.api.FlowActivity;
import org.apache.ode.bom.api.Link;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OFlow;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import java.util.Iterator;


/**
 * Generates code for <code>&lt;flow&gt;</code> activities.
 */
class FlowGenerator extends DefaultActivityGenerator {
  private static final FlowGeneratorMessages __cmsgs = MessageBundle.getMessages(FlowGeneratorMessages.class);

  public void compile(OActivity output, Activity src) {
    FlowActivity flowAct = (FlowActivity)src;
    OFlow oflow = (OFlow) output;
    compileLinkDecls(oflow, flowAct);
    compileChildren(oflow, flowAct);

    for (Iterator<OLink> i = oflow.localLinks.iterator(); i.hasNext(); ) {
      OLink olink = i.next();
      try {
        if (olink.source == null)
          throw new CompilationException(__cmsgs.errLinkMissingSourceActivity(olink.name).setSource(olink));
      } catch (CompilationException ce) {
        _context.recoveredFromError(src, ce);
      }

      try {
        if (olink.target == null)
          throw new CompilationException(__cmsgs.errLinkMissingTargetActivity(olink.name).setSource(olink));
      } catch (CompilationException ce) {
        _context.recoveredFromError(src, ce);
      }
    }

  }

  public OActivity newInstance(Activity src) {
    return new OFlow(_context.getOProcess());
  }

  private void compileLinkDecls(OFlow oflow, FlowActivity flowAct) {
    for (Iterator<Link> i = flowAct.getLinks().iterator(); i.hasNext(); ) {
      Link link = i.next();
      OLink olink = new OLink(_context.getOProcess());
      olink.name = link.getLinkName();
      declareLink(oflow, olink);
    }
  }


  public void declareLink(final OFlow oflow, final OLink olink) throws CompilationException {
    if (CollectionsX.find_if(oflow.localLinks, new MemberOfFunction<OLink>() {
      public boolean isMember(OLink o) {
        return o.name.equals(olink.name);
      }
    }) != null)
      throw new CompilationException(__cmsgs.errDuplicateLinkDecl(olink.name));

    olink.declaringFlow = oflow;
    oflow.localLinks.add(olink);
  }

  /**
   */
  protected void compileChildren(OFlow dest, CompositeActivity src) {
    for (Iterator<Activity> i = src.getChildren().iterator(); i.hasNext();) {
      Activity child = i.next();
      try {
        OActivity compiledChild = _context.compile(child);
        dest.parallelActivities.add(compiledChild);
      } catch (CompilationException ce) {
        _context.recoveredFromError(child, ce);
      }
    }
  }
}
