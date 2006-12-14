package org.apache.ode.test;

public class CompensationHandling20Tests extends BPELTest {

	public void testCompensationHandlers() throws Throwable {
	 go("target/test-classes/bpel/2.0/TestCompensationHandlers");
	}
	
	public void testImplicitFaultHandler() throws Throwable {
		go("target/test-classes/bpel/2.0/TestImplicitFaultHandler");
	}
	
}
