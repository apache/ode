package org.apache.ode.bpel.rtrep.v1;

import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.apache.ode.bpel.rapi.Serializer;
import org.apache.ode.bpel.rapi.OdeRTInstanceContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FilenameFilter;

import junit.framework.TestCase;

/**
 * Reads a whole bunch of Jacob states generated from the 1.X branch and checks
 * that they execute proper.
 */
public class CompatibilityTest extends TestCase {

    public void testReloadProcess() throws Exception {
        File soupsDir = new File(CompatibilityTest.class.getClassLoader().getResource("soups").getFile());
        for (File soupState : soupsDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("sa");
            }
        })) {
            ExecutionQueueImpl soup = new ExecutionQueueImpl(CoreBpelTest.class.getClassLoader());
            Serializer ser = new Serializer(CompatibilityTest.class.getClassLoader().getResourceAsStream("TestActivityFlow.cbp"));
            soup.setReplacementMap(new ReplacementMapImpl((OProcess) ser.readPModel()));

            soup.read(new FileInputStream(soupState));

//            JacobVPU vpu = new JacobVPU(soup);
//            CoreBpelTest instance = new CoreBpelTest();
//            vpu.registerExtension(OdeRTInstanceContext.class, instance);
//
//            instance._completedOk = false;
//            instance._terminate = false;
//            instance._fault = null;
//
//            int i = 0;
//            try {
//            for (i = 0; i < 100000 && !instance._completedOk && instance._fault == null && !instance._terminate; ++i) {
//                vpu.execute();
//            }
//            } catch (Exception npe) {
//                npe.printStackTrace();
//            }
//            System.out.println("=> " + i);
        }
    }

    public void testReloadStates() throws Exception {
        File soupsDir = new File(CompatibilityTest.class.getClassLoader().getResource("soups").getFile());
        for (File soupState : soupsDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("soup");
            }
        })) {
            ExecutionQueueImpl soup = new ExecutionQueueImpl(CoreBpelTest.class.getClassLoader());

            soup.read(new FileInputStream(soupState));
            JacobVPU vpu = new JacobVPU(soup);
            CoreBpelTest instance = new CoreBpelTest();
            vpu.registerExtension(OdeRTInstanceContext.class, instance);

            instance._completedOk = false;
            instance._terminate = false;
            instance._fault = null;

            for (int i = 0; i < 1000 && !instance._completedOk && instance._fault == null && !instance._terminate; ++i)
                vpu.execute();
        }

    }
}
