/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import com.fs.pxe.bom.api.*;
import com.fs.pxe.bom.wsdl.WSDLFactory4BPEL;
import com.fs.pxe.bom.wsdl.WSDLFactoryBPEL11;
import com.fs.pxe.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL11;

/**
 * BPEL v1.1 compiler.
 */
public class BpelCompiler11 extends BpelCompiler {

  /** URI for the XPath 1.0 expression language. */
  public static final String EXPLANG_XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";

  public BpelCompiler11() {
    super((WSDLFactory4BPEL) WSDLFactoryBPEL11.newInstance());

    registerActivityCompiler(EmptyActivity.class, new EmptyGenerator());
    registerActivityCompiler(CompensateActivity.class, new CompensateGenerator());
    registerActivityCompiler(FlowActivity.class, new FlowGenerator());
    registerActivityCompiler(SequenceActivity.class, new SequenceGenerator());
    registerActivityCompiler(AssignActivity.class, new AssignGenerator());
    registerActivityCompiler(ThrowActivity.class, new ThrowGenerator());
    registerActivityCompiler(WhileActivity.class, new WhileGenerator());
    registerActivityCompiler(SwitchActivity.class, new SwitchGenerator());
    registerActivityCompiler(PickActivity.class, new PickGenerator());
    registerActivityCompiler(ReplyActivity.class, new ReplyGenerator());
    registerActivityCompiler(ReceiveActivity.class, new ReceiveGenerator());
    registerActivityCompiler(InvokeActivity.class, new InvokeGenerator());
    registerActivityCompiler(WaitActivity.class, new WaitGenerator());
    registerActivityCompiler(TerminateActivity.class, new TerminateGenerator());

    registerExpressionLanguage(EXPLANG_XPATH, new XPath10ExpressionCompilerBPEL11());
  }

  protected String getBpwsNamespace() {
    return Constants.NS_BPEL4WS_2003_03;
  }

  protected String getDefaultExpressionLanguage() {
    return EXPLANG_XPATH;
  }

}
