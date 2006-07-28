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
package org.apache.ode.sax.bpel;

import org.apache.ode.bom.api.EmptyActivity;
import org.apache.ode.bom.api.Process;
import org.apache.ode.bpel.parser.BpelParseException;
import org.apache.ode.bpel.parser.BpelProcessBuilder;
import org.apache.ode.bpel.parser.BpelProcessBuilderFactory;
import org.apache.ode.sax.fsa.ParseError;
import org.apache.ode.sax.fsa.ParseException;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

public class BpelParserTest extends TestCase {
  BpelProcessBuilderFactory bpf;
  BpelProcessBuilder builder;

  InputSource ok_trivial;
  InputSource bad_trivial;

  public void setUp() throws Exception {
    bpf = BpelProcessBuilderFactory.newProcessBuilderFactory();
    builder = bpf.newBpelProcessBuilder();
    ok_trivial = new InputSource(getClass().getResource("ok_trivial.bpel").toExternalForm());
    bad_trivial = new InputSource(getClass().getResource("bad_trivial.bpel").toExternalForm());
  }

  public void testOkTrivial() throws Exception {
    Process proc = builder.parse(ok_trivial, "<<unknown>>");
    ParseError[] err = builder.getParseErrors();
    assertNotNull(proc);
    assertEquals(0,err.length);
    assertEquals(Process.BPEL_V110, proc.getBpelVersion());
    // These are the schema defaults.
    assertEquals(proc.getQueryLanguage(),"http://www.w3.org/TR/1999/REC-xpath-19991116");
    assertEquals(proc.getExpressionLanguage(),"http://www.w3.org/TR/1999/REC-xpath-19991116");
    assertEquals("empty", proc.getRootActivity().getType());
    assertTrue(EmptyActivity.class.isAssignableFrom(proc.getRootActivity().getClass()));
  }

  public void testBadTrivial() throws Exception {
    try {
      builder.parse(bad_trivial, "<<unknown>>");
      fail("expected exception due to schema violation.");
    } catch (BpelParseException bpe) {
      // validation will fail, and that should get rolled into the ParseContext
      assertTrue(bpe.getCause() instanceof ParseException);
    }
    ParseError[] err = builder.getParseErrors();
    assertTrue(err.length > 0);
  }

}
