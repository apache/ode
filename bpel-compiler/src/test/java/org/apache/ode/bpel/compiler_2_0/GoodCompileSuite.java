/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler_2_0;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * A series of BPEL 2.0  compilation test.
 */
public class GoodCompileSuite extends TestSuite {

  public static Test suite() throws Exception {

    TestSuite suite = new GoodCompileSuite();
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign3-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign5-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign6-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign7-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign8-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/assign/Assign9-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/AsyncProcess/AsyncProcess2.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/compensation/comp1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/compensation/comp2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow3-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow4-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow5-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow6-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/flow/flow7-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/if/If1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/if/If2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/if/If3-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick3-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick4-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick5-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/pick/Pick6-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/rethrow/Rethrow1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/rethrow/Rethrow2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw3-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw4-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw5-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw6-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/throw/Throw7-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/wait/Wait1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/wait/Wait2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/while/While1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData2-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData3-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableData4-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath10-func/GetVariableProperty1-2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableData1-xp2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableData2-xp2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableData3-xp2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableData4-xp2.0.bpel"));
    suite.addTest(new GoodCompileTCase("/2.0/good/xpath20-func/GetVariableProperty1-xp2.0.bpel"));

    return suite;
  }
}
