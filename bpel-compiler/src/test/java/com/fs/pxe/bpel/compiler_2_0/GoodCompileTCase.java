/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler_2_0;

import com.fs.pxe.bpel.capi.CompilationMessage;
import com.fs.pxe.bpel.capi.CompileListener;
import com.fs.pxe.bpel.compiler.BpelC;
import com.fs.utils.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;

import junit.framework.TestCase;


/**
 * JUnit {@link TestCase} of the PXE BPEL compiler. This test case provides
 * is not very complex, it simply ensures that the given BPEL input compiles
 * succesfully. These test cases are intended to be run as part of a suite. 
 */
class GoodCompileTCase extends TestCase implements CompileListener {

  private BpelC _compiler;
  private ArrayList<CompilationMessage> _errors = new ArrayList<CompilationMessage>();
  private URL _bpelURL;

  GoodCompileTCase(String bpel) {
    super(bpel);
    _bpelURL = getClass().getResource(bpel);
  }

  protected void setUp() throws Exception {
    super.setUp();
    _compiler = BpelC.newBpelCompiler();
    _compiler.setCompileListener(this);
    _compiler.setOutputStream(new ByteArrayOutputStream(StreamUtils.DEFAULT_BUFFER_SIZE));
    _errors.clear();
  }

  public void runTest() throws Exception {
    try {
      _compiler.compile(_bpelURL);
    } catch (Exception ex) {
      ex.printStackTrace();
      fail("Compilation did not succeed.");
    }
  }

  public void onCompilationMessage(CompilationMessage compilationMessage) {
    _errors.add(compilationMessage);
    System.err.println(compilationMessage.toString());
  }

}
