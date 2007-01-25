package org.apache.ode.bpel.engine;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

/**
 * Test case for {@link InstanceLockManager}.
 * 
 * @author mszefler
 */
public class InstanceLockManagerTest extends TestCase {

    InstanceLockManager _ilm;

    public void setUp() {
        _ilm = new InstanceLockManager();
    }

    public void testDistinctness() throws Exception {
        _ilm.lock(1L, 0, TimeUnit.MILLISECONDS);
        _ilm.lock(2L, 0, TimeUnit.MILLISECONDS);
        _ilm.unlock(1L);
        _ilm.unlock(2L);
        _ilm.lock(1L, 0, TimeUnit.MILLISECONDS);
        _ilm.lock(2L, 0, TimeUnit.MILLISECONDS);
    }

    public void testExclusion() throws Exception {
        _ilm.lock(1L, 0, TimeUnit.MILLISECONDS);
        try {
            _ilm.lock(1L, 0, TimeUnit.MILLISECONDS);
            fail("Should have timedout.");
        } catch (InstanceLockManager.TimeoutException te) {
            // expected
        }
    }

    public void testWakeUpQueue() throws Exception {
        _ilm.lock(1L, 0, TimeUnit.MICROSECONDS);
        Thread t1 = new TThread();
        Thread t2 = new TThread();
        Thread t3 = new TThread();
        Thread t4 = new TThread();
       
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        
        Thread.sleep(100);
        _ilm.unlock(1L);
                
        t1.join(2000);
        t2.join(2000);
        t3.join(2000);
        t4.join(2000);
        
        assertFalse(t1.isAlive());
        assertFalse(t2.isAlive());
        assertFalse(t3.isAlive());
        assertFalse(t4.isAlive());
        
        
    }

    private class TThread extends Thread {
        public void run() {
            try {
                _ilm.lock(1L, 1000, TimeUnit.MILLISECONDS);
                Thread.sleep(100);
                _ilm.unlock(1L);
            } catch (Exception ex) {
                ex.printStackTrace();
                fail("Unexpected ex");
            }
        }

    }
}
