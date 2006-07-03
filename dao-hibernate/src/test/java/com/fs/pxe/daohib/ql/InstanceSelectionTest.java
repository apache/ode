package com.fs.pxe.daohib.ql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.hibernate.Session;
import org.objectweb.jotm.Jotm;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.fs.pxe.daohib.SessionManager;
import com.fs.pxe.daohib.bpel.hobj.HProcess;
import com.fs.pxe.daohib.bpel.hobj.HProcessInstance;
import com.fs.pxe.daohib.ql.instances.HibernateInstancesQueryCompiler;
import com.fs.pxe.ql.eval.skel.CommandEvaluator;
import com.fs.pxe.ql.tree.Builder;
import com.fs.pxe.ql.tree.BuilderFactory;
import com.fs.pxe.ql.tree.nodes.Query;

public class InstanceSelectionTest extends junit.framework.TestCase {
  public static class TestCases {
    private Collection<TestCase> testCases;

    /**
     * @return the testCases
     */
    public Collection<TestCase> getTestCases() {
      return testCases;
    }

    /**
     * @param testCases the testCases to set
     */
    public void setTestCases(Collection<TestCase> testCases) {
      this.testCases = testCases;
    }
    
  }
  public static class TestCase {
    private Collection<HProcess> processes;
    private Collection<HProcessInstance> instances;
    private String query;
    private int resultSetSize;
    /**
     * @return the instances
     */
    public Collection<HProcessInstance> getInstances() {
      return instances;
    }
    /**
     * @param instances the instances to set
     */
    public void setInstances(Collection<HProcessInstance> instances) {
      this.instances = instances;
    }
    /**
     * @return the processes
     */
    public Collection<HProcess> getProcesses() {
      return processes;
    }
    /**
     * @param processes the processes to set
     */
    public void setProcesses(Collection<HProcess> processes) {
      this.processes = processes;
    }
    /**
     * @return the query
     */
    public String getQuery() {
      return query;
    }
    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
      this.query = query;
    }
    /**
     * @return the resultSetSize
     */
    public int getResultSetSize() {
      return resultSetSize;
    }
    /**
     * @param resultSetSize the resultSetSize to set
     */
    public void setResultSetSize(int resultSetSize) {
      this.resultSetSize = resultSetSize;
    }
    
  }
  private static final String BEANS_LOCATION = "test/config/test-beans.xml";
  private static final String HIBERNATE_PROPERTIES = "hibernate.properties";
  //
  private Jotm jotm;
  private SessionManager sessionManager;
  //private BpelDAOConnectionFactory bpelConnFactory;
  private Session session;
  private TestCases testCases;
  
  private Properties readDBProperties() throws IOException {
    Properties props = new Properties();
    
    new File("tmp").delete();
    
    props.put("hibernate.connection.url", "jdbc:derby:tmp/dao-hibernate-test;create=true;");
    
    InputStream is = InstanceSelectionTest.class.getClassLoader().getResourceAsStream(HIBERNATE_PROPERTIES);
    try {
      props.load(is);
    } finally {
      is.close();
    }
    return props;
  }
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    jotm = new Jotm(true, false);
    
    sessionManager = new SessionManager(readDBProperties(), jotm.getTransactionManager());
    /*
    bpelConnFactory = new BpelDAOConnectionFactoryImpl(sessionManager);
    
    BpelDAOConnection bpelConn = bpelConnFactory.createConnection();
    */
    jotm.getTransactionManager().begin();

    session = sessionManager.getSession();
    session.beginTransaction();
    //init test cases
    Resource res = new FileSystemResource(BEANS_LOCATION);
    XmlBeanFactory factory = new XmlBeanFactory(res);
    testCases = (TestCases)factory.getBean("testCases");

  }

  private void runTestCase(TestCase testCase) throws Exception {
    //TODO clean data
    
    //init data for test
    for(HProcess process : testCase.getProcesses()) {
      session.saveOrUpdate(process);
    }
    for(HProcessInstance instance : testCase.getInstances()) {
      session.saveOrUpdate(instance);
    }
    //run query
    Builder<String> builder = BuilderFactory.getInstance().createBuilder();
    final com.fs.pxe.ql.tree.nodes.Node rootNode = builder.build(testCase.getQuery());
    
    HibernateInstancesQueryCompiler compiler = new HibernateInstancesQueryCompiler();
    
    CommandEvaluator<List, Session> eval = compiler.compile((Query)rootNode);
    List<HProcessInstance> instancesList = (List<HProcessInstance>)eval.evaluate(session);
    
     assertEquals(instancesList.size(), testCase.getResultSetSize());
  }
  
  public void test() throws Exception {
    for(TestCase testCase : testCases.getTestCases()) {
      runTestCase(testCase);
    }
  }
}
