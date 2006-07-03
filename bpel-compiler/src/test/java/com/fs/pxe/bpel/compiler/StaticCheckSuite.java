/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * JUnit {@link TestSuite} for running a series of {@link StaticCheckTCase}s.
 */
public class StaticCheckSuite extends TestSuite {

  public static Test suite() throws Exception {
    TestSuite suite = new StaticCheckSuite();
    suite.addTest(new StaticCheckTCase("BpelParseErr"));
    suite.addTest(new StaticCheckTCase("PortTypeMismatch"));
    suite.addTest(new StaticCheckTCase("UndeclaredPropertyAlias"));
    suite.addTest(new StaticCheckTCase("UnknownBpelFunction"));
    suite.addTest(new StaticCheckTCase("UndeclaredVariable"));
    suite.addTest(new StaticCheckTCase("DuplicateLinkTarget"));
    suite.addTest(new StaticCheckTCase("DuplicateLinkSource"));
    suite.addTest(new StaticCheckTCase("DuplicateLinkDecl"));
    suite.addTest(new StaticCheckTCase("LinkMissingSourceActivity"));
    suite.addTest(new StaticCheckTCase("LinkMissingTargetActivity"));
    suite.addTest(new StaticCheckTCase("DuplicateVariableDecl"));
    // We simply can't test the next one without using the BOM; both the parser
    // and schema validation would rule it out.
    //suite.addTest(new StaticCheckTest("CompensateNAtoContext"));
    return suite;
  }
}
