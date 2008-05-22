/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.utils.msg.MessageBundle;

import java.net.URI;
import java.util.Locale;

import junit.framework.TestCase;

public class CompilationMessageTest extends TestCase {

  private static String NO_PARAMETER = "No parameter!";
  private static String NO_PARAMETER_CODE = "NoParameter";
  private static String NO_PARAMETER_DE = "Kein Parameter!";
  private static String WRONG_PARAMETER = "Wrong parameter :-)";
  private static String WRONG_PARAMETER_CODE = "WrongParameter";

  private CompilationTestMessages _bundle;
  private SourceLocationImpl sloc;
  private Locale oldLocale;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    oldLocale = Locale.getDefault();
    Locale.setDefault(Locale.ENGLISH);
    _bundle = MessageBundle.getMessages(CompilationTestMessages.class);
    sloc = new SourceLocationImpl(new URI("urn:foo"));
  }

  @Override
  protected void tearDown() throws Exception {
    _bundle = null;
    Locale.setDefault(oldLocale);
    super.tearDown();
  }

  public void testNoParameter() {
    CompilationMessage msg = _bundle.infNoParameter().setSource(sloc);
    assertEquals(NO_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.INFO, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(sloc, msg.source);

    msg = _bundle.warnNoParameter().setSource(sloc);
    assertEquals("No parameter!", msg.messageText);
    assertEquals(CompilationMessage.WARN, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(sloc, msg.source);

    msg = _bundle.errNoParameter().setSource(sloc);
    assertEquals(NO_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.ERROR, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(sloc, msg.source);
  }

  public void testNoParameterLocalized() {
    CompilationTestMessages bundle = MessageBundle.getMessages(
        CompilationTestMessages.class, Locale.GERMAN);
    CompilationMessage msg = bundle.infNoParameter().setSource(sloc);
    assertEquals(NO_PARAMETER_DE, msg.messageText);
    assertEquals(CompilationMessage.INFO, msg.severity);
    assertEquals(NO_PARAMETER_CODE, msg.code);
    assertSame(sloc, msg.source);
  }

  public void testWrongParameter() {
    CompilationMessage msg = _bundle.infWrongParameter(":-)").setSource(sloc);
    assertEquals(WRONG_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.INFO, msg.severity);
    assertEquals(WRONG_PARAMETER_CODE, msg.code);
    assertSame(sloc, msg.source);

    msg = _bundle.warnWrongParameter(":-)").setSource(sloc);
    assertEquals(WRONG_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.WARN, msg.severity);
    assertEquals(WRONG_PARAMETER_CODE, msg.code);
    assertSame(sloc, msg.source);

    msg = _bundle.errWrongParameter(":-)").setSource(sloc);
    assertEquals(WRONG_PARAMETER, msg.messageText);
    assertEquals(CompilationMessage.ERROR, msg.severity);
    assertEquals(WRONG_PARAMETER_CODE, msg.code);
    assertSame(sloc, msg.source);
  }

  public void testWrongMethod() {
    try {
      _bundle.msgWrongMethod().setSource(sloc);
      fail("should have failed on msgWrongMethod()");
    }
    catch (UnsupportedOperationException uop) {
      // OK
    }
  }

}
