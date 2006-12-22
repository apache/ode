package org.apache.ode.test;

public class FaultHandling20Test extends BPELTest {
	public void testFaultHandlers() throws Throwable {
		go("target/test-classes/bpel/2.0/TestFaultHandlers");
	}
    public void testFaultWithVariable() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestFaultWithVariable");
    }
	public void testCatchFaultInFaultHandler() throws Throwable {
		go("target/test-classes/bpel/2.0/TestCatchFaultInFaultHandler");
	}
}
