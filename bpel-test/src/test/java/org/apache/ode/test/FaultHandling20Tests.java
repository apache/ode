package org.apache.ode.test;

public class FaultHandling20Tests extends BPELTest {
	public void testFaultHandlers() throws Exception {
		go("target/test-classes/bpel/2.0/TestFaultHandlers");
	}
    public void testFaultWithVariable() throws Exception {
    	go("target/test-classes/bpel/2.0/TestFaultWithVariable");
    }
	public void testCatchFaultInFaultHandler() throws Exception {
		go("target/test-classes/bpel/2.0/TestCatchFaultInFaultHandler");
	}
}
