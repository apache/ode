/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.bpel.capi;

import com.fs.utils.msg.MessageBundle;

import java.util.Locale;

import junit.framework.TestCase;

public class CompilationMessageTest extends TestCase {

  private static String NO_PARAMETER = "No parameter!";
  private static String NO_PARAMETER_CODE = "NoParameter";
  private static String NO_PARAMETER_DE = "Kein Parameter!";
  private static String WRONG_PARAMETER = "Wrong parameter :-)";
  private static String WRONG_PARAMETER_CODE = "WrongParameter";

  private CompilationTestMessages _bundle;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _bundle = MessageBundle.getMessages(CompilationTestMessages.class);
  }

  @Override
  protected void tearDown() throws Exception {
    _bundle = null;
    super.tearDown();
  }

  public void testNoParameter() {
    CompilationMessage msg = _bundle.infNoParameter().setSource(this);
    assertEquals(NO_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.INFO, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(this, msg.source);

    msg = _bundle.warnNoParameter().setSource(this);
    assertEquals("No parameter!", msg.messageText);
    assertEquals(CompilationMessage.WARN, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(this, msg.source);

    msg = _bundle.errNoParameter().setSource(this);
    assertEquals(NO_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.ERROR, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(this, msg.source);
  }

  public void testNoParameterLocalized() {
    CompilationTestMessages bundle = MessageBundle.getMessages(
        CompilationTestMessages.class, Locale.GERMAN);
    CompilationMessage msg = bundle.infNoParameter().setSource(this);
    assertEquals(NO_PARAMETER_DE, msg.messageText);
    assertEquals(CompilationMessage.INFO, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(this, msg.source);
  }

  public void testWrongParameter() {
    CompilationMessage msg = _bundle.infWrongParameter(":-)").setSource(this);
    assertEquals(WRONG_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.INFO, msg.severity);
    assertEquals(WRONG_PARAMETER_CODE, msg.code);
    assertSame(this, msg.source);

    msg = _bundle.warnWrongParameter(":-)").setSource(this);
    assertEquals(WRONG_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.WARN, msg.severity);
    assertEquals(WRONG_PARAMETER_CODE, msg.code);
    assertSame(this, msg.source);

    msg = _bundle.errWrongParameter(":-)").setSource(this);
    assertEquals(WRONG_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.ERROR, msg.severity);
    assertEquals(WRONG_PARAMETER_CODE, msg.code);
    assertSame(this, msg.source);
  }

  public void testWrongMethod() {
    try {
      _bundle.msgWrongMethod().setSource(this);
      fail("should have failed on msgWrongMethod()");
    }
    catch (UnsupportedOperationException uop) {
      // OK
    }
  }

}
