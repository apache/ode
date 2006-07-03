/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.utils.cli;

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
