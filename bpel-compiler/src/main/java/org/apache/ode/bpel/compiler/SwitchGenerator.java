package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.SwitchActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OSwitch;

/**
 * Generator for legacy BPEL 1.1 <code>&lt;switch&gt;</code> actiivty.
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class SwitchGenerator extends DefaultActivityGenerator {
  public OActivity newInstance(Activity src) {
        return new OSwitch(_context.getOProcess(), _context.getCurrent());
  }

  public void compile(OActivity output, Activity src) {
    OSwitch oswitch = (OSwitch) output;
    SwitchActivity switchDef = (SwitchActivity)src;

    for (SwitchActivity.Case ccase : switchDef.getCases()) {
      OSwitch.OCase ocase = new OSwitch.OCase(_context.getOProcess());
      ocase.activity = _context.compile(ccase.getActivity());
      ocase.expression = (ccase.getCondition() == null ? _context.constantExpr(true) : _context.compileExpr(ccase.getCondition()));
      oswitch.addCase(ocase);
    }
  }
}