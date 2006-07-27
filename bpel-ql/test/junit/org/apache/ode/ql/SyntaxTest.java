package org.apache.ode.ql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.ode.ql.tree.Builder;
import org.apache.ode.ql.tree.BuilderFactory;

public class SyntaxTest extends TestCase {

  private final static String TESTS_DIR = "./test/test-cases";
  
  private File[] casesFiles = new File(TESTS_DIR).listFiles();
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
      super.setUp();
      
      casesFiles = new File(TESTS_DIR).listFiles();
  }
  
  public void test() throws Exception {
      for(File caseFile : casesFiles) {
        doTestCaseFile(caseFile);
      }
  }
  
  public void doTestCaseFile(File caseFile) throws Exception {
    BufferedReader reader = new BufferedReader(new FileReader(caseFile));
    try {
      String testCase;
      while((testCase = reader.readLine())!=null) {
        try {
          doTest(testCase);
        }catch(Throwable ex) {
          throw new Exception("Failure in "+caseFile.toString()+":" + testCase + " case.", ex);
        }

      }
    }finally {
      reader.close();
    }
  }
  
  public void doTest(String query) throws Exception {
    Builder<String> builder = BuilderFactory.getInstance().createBuilder();
    final org.apache.ode.ql.tree.nodes.Node rootNode = builder.build(query);
  }
  
}
