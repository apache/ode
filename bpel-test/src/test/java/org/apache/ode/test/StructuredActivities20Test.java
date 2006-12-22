package org.apache.ode.test;

public class StructuredActivities20Test extends BPELTest {
	public void testFlowActivity1() throws Throwable {
		// Test Flow with XPath20
		go("target/test-classes/bpel/2.0/TestFlowActivity1");
	}
	public void testFlowActivity2() throws Throwable {
		// Test Flow with XPath10
		go("target/test-classes/bpel/2.0/TestFlowActivity2");
	}
}
