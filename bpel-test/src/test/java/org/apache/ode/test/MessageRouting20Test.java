package org.apache.ode.test;

public class MessageRouting20Test extends BPELTestAbstract {

	public void testCorrelation() throws Throwable {
		go("target/test-classes/bpel/2.0/TestCorrelation");
	}
	public void testCorrelation1() throws Throwable {
		go("target/test-classes/bpel/2.0/TestCorrelation1");
	}
	public void testCorrelationOpaque() throws Throwable {
		go("target/test-classes/bpel/2.0/testCorrelationOpaque");
	}
	public void testCorrelationAsync() throws Throwable {
		go("target/test-classes/bpel/2.0/TestCorrelationAsync");
	}
    public void testDynamicPick() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestDynamicPick");
    }
    public void testInstPick() throws Throwable {
        go("target/test-classes/bpel/2.0/TestInstantiatingPick");
    }
    public void testStaticOnMessage() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestStaticOnMessage");
    }
    public void testStaticPick() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestStaticPick");
    }
	public void testNegativeCorrelation() throws Throwable {
			/**
			 * This test contains invalid BPEL. There is an instantiating
			 * <receive> and a subsequent <pick> that does not define a correlation
			 * key. The BPEL compiler should throw an exception indicating
			 * the BPEL code error ( verify with spec ).
			 * 
			 * See JIRA ODE-64
			 * 
			 */
	   negative("target/test-classes/bpel/2.0/NegativeCorrelationTest");
	}
	  public void testNegativeInitialization() throws Throwable {
			/**
			 * This test contains invalid BPEL. There is an instantiating
			 * <receive> within a <scope>. The <scope> contains eventhandlers
			 * that reference the correlation set found on the receive. The BPEL
			 * compiler should throw an exception indicating
			 * the BPEL error ( verify with spec ) or at runtime
			 * a clear initialization exception should be thrown.
			 * 
			 * See JIRA ODE-61.
			 * 
			 * The message exchange should return with a Fault/Failure.
			 * 
			 */
		    negative("target/test-classes/bpel/2.0/NegativeInitializationTest");
	   }

}
