/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package org.apache.ode.utils;

/**
 * Extensions for java.lang.System
 */

public class SystemUtils {

  /**
   * @see System#getProperties()
   */
  public static String javaVersion() {
    return System.getProperty("java.version");
  }

  /**
   * @see System#getProperties()
   */
  public static String javaVendor() {
    return System.getProperty("java.vendor");
  }

  /**
   * @see System#getProperties()
   */
  public static String javaHome() {
    return System.getProperty("java.home");
  }

  /**
   * @see System#getProperties()
   */
  public static String javaClassVersion() {
    return System.getProperty("java.class.version");
  }

  /**
   * @see System#getProperties()
   */
  public static String javaClassPath() {
    return System.getProperty("java.class.path");
  }

  /**
   * @see System#getProperties()
   */
  public static String javaTemporaryDirectory() {
    return System.getProperty("java.io.tmpdir");
  }

  /**
   * @see System#getProperties()
   */
  public static String javaLibraryPath() {
    return System.getProperty("java.library.path");
  }

  /**
   * @see System#getProperties()
   */
  public static String operatingSystemArchitecture() {
    return System.getProperty("os.arch");
  }

  /**
   * @see System#getProperties()
   */
  public static String operatingSystemName() {
    return System.getProperty("os.name");
  }

  /**
   * @see System#getProperties()
   */
  public static String operatingSystemVersion() {
    return System.getProperty("os.version");
  }

  /**
   * @see System#getProperties()
   */
  public static String fileSeparator() {
    return System.getProperty("file.separator");
  }

  /**
   * @see System#getProperties()
   */
  public static String pathSeparator() {
    return System.getProperty("path.separator");
  }

  /**
   * @see System#getProperties()
   */
  public static String lineSeparator() {
    return System.getProperty("line.separator");
  }

  /**
   * @see System#getProperties()
   */
  public static String userName() {
    return System.getProperty("user.name");
  }

  /**
   * @see System#getProperties()
   */
  public static String userHome() {
    return System.getProperty("user.home");
  }

  /**
   * @see System#getProperties()
   */
  public static String userDirectory() {
    return System.getProperty("user.dir");
  }

}
