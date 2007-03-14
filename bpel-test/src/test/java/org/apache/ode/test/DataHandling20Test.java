package org.apache.ode.test;

public class DataHandling20Test extends BPELTestAbstract {

    public void testXPathNamespace1() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestXPathNamespace1");
    }
    public void testXPathNamespace2() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestXPathNamespace2");
    }
	public void testSubTreeAssign() throws Throwable {
		go("target/test-classes/bpel/2.0/TestSubTreeAssign");
	}
    public void testAssignActivity1() throws Throwable {
        go("src/test/resources/bpel/2.0/TestAssignActivity1");
    }
    public void testAssignActivity2() throws Throwable {
        go("src/test/resources/bpel/2.0/TestAssignActivity2");
    }
    public void testSimpleTypeParts() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestSimpleTypeParts");
    }
    public void testSimpleVariableType() throws Throwable {
    	go("target/test-classes/bpel/2.0/TestSimpleVariableType");
    }
    public void testXslTransform() throws Throwable {
        go("target/test-classes/bpel/2.0/TestXslTransform");
    }
	
}
