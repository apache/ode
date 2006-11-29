package org.apache.ode.store;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.ode.bpel.iapi.ProcessConf;

public class ProcessStoreTest extends TestCase {

    ProcessStoreImpl _ps;
    private File _testdd;
    
    public void setUp() throws Exception {
        _ps = new ProcessStoreImpl(null);
        _ps.loadAll();
        URL tdd= getClass().getResource("/testdd/deploy.xml");
        _testdd = new File(tdd.getPath()).getParentFile();
    } 
    
    public void tearDown() throws Exception {
        _ps.shutdown();
    }
     
    public void testSanity() {
        assertEquals(0,_ps.getProcesses().size());
        assertEquals(0,_ps.getPackages().size());
        assertNull(_ps.listProcesses("foobar"));
    }
    
    public void testDeploy() {
        Collection<QName> deployed = _ps.deploy(_testdd);
        assertNotNull(deployed);
        assertEquals(1,deployed.size());
    }
    
    public void testGetProcess() {
        Collection<QName> deployed = _ps.deploy(_testdd);
        QName pname = deployed.iterator().next();
        assertNotNull(deployed);
        assertEquals(1,deployed.size());
        ProcessConf pconf = _ps.getProcessConfiguration(pname);
        assertNotNull(pconf);
        assertEquals(_testdd.getName(),pconf.getPackage());
        assertEquals(pname, pconf.getProcessId());
    } 
    
    public void testGetProcesses() {
        Collection<QName> deployed = _ps.deploy(_testdd);
        QName pname = deployed.iterator().next();
        assertNotNull(deployed);
        assertEquals(1,deployed.size());
        List<QName> pconfs = _ps.getProcesses();
        assertEquals(pname,pconfs.get(0));
    }
    
    
}
