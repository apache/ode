/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.ql;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import junit.framework.TestCase;

import org.apache.ode.ql.tree.Builder;
import org.apache.ode.ql.tree.BuilderFactory;

public class SyntaxTest extends TestCase {

  private final static String TESTS_DIR = "target/test-classes";
  
  private File[] casesFiles = new File(TESTS_DIR).listFiles();
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
      super.setUp();
      
      casesFiles = new File(System.getProperty("baseDir")).listFiles();
      //casesFiles = new File(TESTS_DIR).listFiles();
  }
  
  public void test() throws Exception {
      for(File caseFile : casesFiles) {
      if (caseFile.isFile()) {
        doTestCaseFile(caseFile);
        }
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
    @SuppressWarnings("unused")
	final org.apache.ode.ql.tree.nodes.Node rootNode = builder.build(query);
  }
  
}
