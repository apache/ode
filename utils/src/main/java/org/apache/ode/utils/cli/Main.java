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
package org.apache.ode.utils.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * <p>
 * Class used for starting the <code>main(...)</code> method of command-line
 * utilities. Provides a general startup mechanism.
 * </p>
 * <p>
 */
public class Main {
  /**
   * The name of the system property that is exepcted to contain the actual main class name.
   * The main-class is a normal Java program. The main-class will be loaded using the bootstrap class loader.
   */
  public static final String PROP_MAINCLASS = Main.class.getName() + ".mainClass";

  public static void main(String args[]) throws Throwable {
    if (args.length == 0) {
      throw new IllegalArgumentException(
          "LAUNCHER FAILURE: A configuration file must be supplied as an argument.");
    }
    File propertyFile = new File(args[0]);
    if (!propertyFile.exists()) {
      throw new IllegalArgumentException("BOOTSTRAP FAILURE: No such file " + propertyFile);
    }

    Properties props = new Properties();
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(propertyFile);
      props.load(fis);
      fis.close();
    } catch (IOException ioex) {
      throw new RuntimeException(ioex);
    }

    String mainClassName = props.getProperty(PROP_MAINCLASS);
    if (mainClassName == null) {
      throw new IllegalArgumentException("BOOTSTRAP FAILURE: The configuration file" +
          propertyFile + " does not set the " + PROP_MAINCLASS + " property.");
    }

    String[] realArgs = new String[args.length-1];
    System.arraycopy(args, 1, realArgs, 0, realArgs.length);

    Class mainClass = Class.forName(mainClassName);

    Method mainMethod = mainClass.getMethod("main",new Class[] { String[].class } );

    try {
      mainMethod.invoke(mainClass,  new Object[] {realArgs});
    } catch (InvocationTargetException ite) {
      throw ite.getTargetException();
    }
  }


}
