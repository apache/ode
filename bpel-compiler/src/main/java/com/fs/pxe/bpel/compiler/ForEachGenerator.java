package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.Activity;
import com.fs.pxe.bom.api.Constants;
import com.fs.pxe.bom.api.ForEachActivity;
import com.fs.pxe.bom.impl.nodes.VariableImpl;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.pxe.bpel.o.OActivity;
import com.fs.pxe.bpel.o.OForEach;
import com.fs.pxe.bpel.o.OScope;
import com.fs.utils.msg.MessageBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Generates code for <code>&lt;forEach&gt;</code> activities.
 */
public class ForEachGenerator extends DefaultActivityGenerator {

  private static final Log __log = LogFactory.getLog(AssignGenerator.class);
  private static final ForEachGeneratorMessages __cmsgs = MessageBundle.getMessages(ForEachGeneratorMessages.class);

  public OActivity newInstance(Activity src) {
    return new OForEach(_context.getOProcess());
  }

  public void compile(OActivity output, Activity src) {
    if (__log.isDebugEnabled()) __log.debug("Compiling ForEach activity.");
    OForEach oforEach = (OForEach) output;
    ForEachActivity forEach = (ForEachActivity) src;
    oforEach.parallel = forEach.isParallel();
    oforEach.startCounterValue = _context.compileExpr(forEach.getStartCounter());
    oforEach.finalCounterValue = _context.compileExpr(forEach.getFinalCounter());
    if (forEach.getCompletionCondition() != null) {
      oforEach.completionCondition =
              new OForEach.CompletionCondition(_context.getOProcess());
      oforEach.completionCondition.successfulBranchesOnly
              = forEach.getCompletionCondition().isSuccessfulBranchesOnly();
      oforEach.completionCondition.branchCount = _context.compileExpr(forEach.getCompletionCondition());
    }

    // ForEach 'adds' a counter variable in inner scope
    if (__log.isDebugEnabled()) __log.debug("Adding the forEach counter variable to inner scope.");
    addCounterVariable(forEach.getCounterName(), forEach);

    if (__log.isDebugEnabled()) __log.debug("Compiling forEach inner scope.");
    oforEach.innerScope = (OScope) _context.compileSLC(forEach.getScope());

    oforEach.counterVariable = oforEach.innerScope.getLocalVariable(forEach.getCounterName());
  }

  private void addCounterVariable(String counterName, ForEachActivity src) {
    // Checking if a variable using the same name as our counter is already defined.
    // The spec requires a static analysis error to be thrown in that case.
    if (src.getScope().getVariableDecl(counterName) != null)
      throw new CompilationException(__cmsgs.errForEachAndScopeVariableRedundant(counterName).setSource(src));

    QName varTypeName = new QName(Constants.NS_XML_SCHEMA_2001, "unsignedInt");
    VariableImpl var = new VariableImpl(counterName);
    var.setSchemaType(varTypeName);
    src.getScope().addVariable(var);

    if (__log.isDebugEnabled())
      __log.debug("forEach counter variable " + counterName + " has been added to inner scope.");
  }

}
