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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerFactory;

import junit.framework.TestCase;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompileListener;
import org.apache.ode.utils.xsl.XslTransformHandler;


/**
 * JUnit {@link TestCase} of static-analysis checking in the ODE BPEL compiler.
 * These test cases are intended to be run as part of a suite. Each test case
 * instance is used to test a particular detectable error condition.
 */
class StaticCheckTCase extends TestCase implements CompileListener {

  private int idx = 0;
  private String name;
  private BpelC _compiler;
  private List<CompilationMessage> _errors = new ArrayList<CompilationMessage>();
  private Set<InputStream> _streams = new HashSet<InputStream>();
  private URL _bpelURL;
  private String _wsdlURI;

  StaticCheckTCase(String name) {
    super(name);
    this.name = name;
  }

  public StaticCheckTCase(String name, int idx) {
    super(name + idx);
    this.name = name;
    this.idx = idx;
  }

  protected void setUp() throws Exception {
    super.setUp();
    TransformerFactory trsf = new net.sf.saxon.TransformerFactoryImpl();
    XslTransformHandler.getInstance().setTransformerFactory(trsf);
    _compiler = BpelC.newBpelCompiler();
    _compiler.setCompileListener(this);
    _errors.clear();

    String baseFname = name + ((idx > 0)
                               ? Integer.toString(idx)
                               : "");
    _bpelURL = getClass().getResource(baseFname + ".bpel");
    _wsdlURI = (baseFname + ".wsdl");

    _compiler.setProcessWSDL(new URI(_wsdlURI));
  }

  protected void tearDown() throws Exception {
    for (InputStream s: _streams) {
      s.close();
    }
    _streams.clear();
    super.tearDown();
  }

  public void runTest() throws Exception {
    try {
      _compiler.compile(new File(_bpelURL.toURI()), 1L);
      fail("Expected compilation exception.");
    } catch (CompilationException ce) {
      _errors.add(ce.getCompilationMessage());
    }

    assertTrue(_errors.size()!=0);

    boolean found = false;
    for (Iterator<CompilationMessage> i = _errors.iterator(); i.hasNext(); ) {
      CompilationMessage msg = i.next();
      if (msg.severity == CompilationMessage.ERROR && msg.code.equals(name)) {
        found = true;
      }
    }

    assertTrue("Expected error \"" + name + "\" not found in " + _errors, found);
  }

  public void onCompilationMessage(CompilationMessage compilationMessage) {
    _errors.add(compilationMessage);
  }

  public void setBaseURI(URI u) {
  }
  
}
