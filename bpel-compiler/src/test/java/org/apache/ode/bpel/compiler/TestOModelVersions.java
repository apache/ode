package org.apache.ode.bpel.compiler;

import junit.framework.TestCase;
import org.apache.ode.bpel.rapi.Serializer;
import org.apache.ode.bpel.rapi.ProcessModel;

public class TestOModelVersions extends TestCase {

    public void testVersionReload() throws Exception {
        Serializer ser = new Serializer(getClass().getClassLoader().getResourceAsStream("TestActivityFlow-v1.cbp"));
        ProcessModel op = ser.readPModel();
        assertEquals("org.apache.ode.bpel.rtrep.v1.OProcess", op.getClass().getName());

        ser = new Serializer(getClass().getClassLoader().getResourceAsStream("TestActivityFlow-v2.cbp"));
        op = ser.readPModel();
        assertEquals("org.apache.ode.bpel.rtrep.v2.OProcess", op.getClass().getName());
    }
}
