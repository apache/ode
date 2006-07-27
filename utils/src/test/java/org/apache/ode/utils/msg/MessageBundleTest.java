/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils.msg;

import java.util.Locale;

import junit.framework.TestCase;

public class MessageBundleTest extends TestCase {

  private static String NO_PARAMETER = "No parameter!";
  private static String NO_PARAMETER_DE = "Kein Parameter!";
  private static String WRONG_PARAMETER = "Wrong parameter :-)";

  private MessageBundleTestMessages _bundle;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    _bundle = MessageBundle.getMessages(MessageBundleTestMessages.class);
  }

  @Override
  protected void tearDown() throws Exception {
    _bundle = null;
    super.tearDown();
  }

  public void testNoParameter() {
    assertEquals(NO_PARAMETER, _bundle.msgNoParameter());
  }

  public void testNoParameterLocalized() {
    MessageBundleTestMessages bundle = MessageBundle.getMessages(
        MessageBundleTestMessages.class, Locale.GERMAN);
    assertEquals(NO_PARAMETER_DE, bundle.msgNoParameter());
  }

  public void testWrongParameter() {
    assertEquals(WRONG_PARAMETER, _bundle.msgWrongParameter(":-)"));
  }

}
