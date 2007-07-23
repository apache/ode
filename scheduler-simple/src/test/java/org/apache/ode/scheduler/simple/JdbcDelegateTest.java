package org.apache.ode.scheduler.simple;

import java.util.HashMap;
import java.util.List;

import org.apache.ode.scheduler.simple.DatabaseDelegate;
import org.apache.ode.scheduler.simple.Job;


import junit.framework.TestCase;

/**
 * 
 * Test of the JDBC delegate. 
 * 
 * @author Maciej Szefler ( m s z e f l e r  @ g m a i l . c o m )
 */
public class JdbcDelegateTest extends TestCase {

    private DelegateSupport _ds;
    private DatabaseDelegate _del;

    public void setUp() throws Exception {
        _ds = new DelegateSupport();
        _del = _ds.delegate();
    }
    
    
    public void testGetNodeIds() throws Exception {
        // should have no node ids in the db, empty list (not null)
        List<String> nids = _del.getNodeIds();
        assertNotNull(nids);
        assertEquals(0, nids.size());
        
        // try for one nodeid
        _del.insertJob(new Job(0L,true,new HashMap<String, Object>()), "abc", true);
        nids = _del.getNodeIds();
        assertEquals(1, nids.size());
        assertTrue(nids.contains("abc"));
        
        // check that dups are ignored. 
        _del.insertJob(new Job(0L,true,new HashMap<String, Object>()), "abc", true);    
        nids = _del.getNodeIds();
        assertEquals(1, nids.size());
        assertTrue(nids.contains("abc"));
        
        // add another nodeid, 
        _del.insertJob(new Job(0L,true,new HashMap<String, Object>()), "123", true);    
        nids = _del.getNodeIds();
        assertEquals(2, nids.size());
        assertTrue(nids.contains("abc"));        
        assertTrue(nids.contains("123"));        
    }

    public void testReassign() throws Exception {
        _del.insertJob(new Job(100L,"j1",true,new HashMap<String, Object>()), "n1", false);
        _del.insertJob(new Job(200L,"j2",true,new HashMap<String, Object>()), "n2", false);
        
        assertEquals(1,_del.updateReassign("n1","n2"));
        List<Job> jobs = _del.dequeueImmediate("n2", 400L, 1000);
        assertEquals(2,jobs.size());
    }

    public void testScheduleImmediateTimeFilter() throws Exception {
        _del.insertJob(new Job(100L,"j1",true,new HashMap<String, Object>()), "n1", false);
        _del.insertJob(new Job(200L,"j2",true,new HashMap<String, Object>()), "n1", false);


        List<Job> jobs = _del.dequeueImmediate("n1", 150L, 1000);
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("j1",jobs.get(0).jobId);
        jobs = _del.dequeueImmediate("n1", 250L, 1000);
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("j2",jobs.get(0).jobId);
    }
    
    public void testScheduleImmediateMaxRows() throws Exception {
        _del.insertJob(new Job(100L,"j1",true,new HashMap<String, Object>()), "n1", false);
        _del.insertJob(new Job(200L,"j2",true,new HashMap<String, Object>()), "n1", false);

        List<Job> jobs = _del.dequeueImmediate("n1", 201L, 1);
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("j1",jobs.get(0).jobId);
        jobs = _del.dequeueImmediate("n1", 250L, 1000);
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("j2",jobs.get(0).jobId);
    }

    public void testScheduleImmediateNodeFilter() throws Exception {
        _del.insertJob(new Job(100L,"j1",true,new HashMap<String, Object>()), "n1", false);
        _del.insertJob(new Job(200L,"j2",true,new HashMap<String, Object>()), "n2", false);

        List<Job> jobs = _del.dequeueImmediate("n2", 300L, 1000);
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        assertEquals("j2",jobs.get(0).jobId);
    }

    public void testDeleteJob() throws Exception {
        _del.insertJob(new Job(100L,"j1",true,new HashMap<String, Object>()), "n1", false);
        _del.insertJob(new Job(200L,"j2",true,new HashMap<String, Object>()), "n2", false);
        
        // try deleting, wrong jobid -- del should fail
        assertFalse(_del.deleteJob("j1x", "n1"));
        assertEquals(2,_del.getNodeIds().size());

        // wrong nodeid
        assertFalse(_del.deleteJob("j1", "n1x"));
        assertEquals(2,_del.getNodeIds().size());
        
        // now do the correct job
        assertTrue(_del.deleteJob("j1", "n1"));
        assertEquals(1,_del.getNodeIds().size());
    }
    
    public void testUpgrade() throws Exception {
        for (int i = 0; i < 200; ++i)
            _del.insertJob(new Job(i ,"j" +i,true,new HashMap<String, Object>()), null, false);
        
        int n1 = _del.updateAssignToNode("n1", 0, 3, 100);
        int n2 = _del.updateAssignToNode("n2", 1, 3, 100);
        int n3 = _del.updateAssignToNode("n3", 2, 3, 100);
        // Make sure we got 100 upgraded nodes
        assertEquals(100,n1+n2+n3);
        
        // now do scheduling. 
        assertEquals(n1,_del.dequeueImmediate("n1", 10000L, 1000).size());
        assertEquals(n2,_del.dequeueImmediate("n2", 10000L, 1000).size());
        assertEquals(n3,_del.dequeueImmediate("n3", 10000L, 1000).size());
    }
    
}
