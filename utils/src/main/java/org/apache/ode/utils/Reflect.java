/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils;

import java.lang.reflect.Method;

public class Reflect {
  
  /**
   * Generates a unique method signature string for a java.lang.reflect.Method.
   * Necessary b/c Method is not Serializable.
   * @param method
   * @return
   */
	public static String generateMethodSignature(Method method) {
    StringBuffer sb = new StringBuffer(64);

    sb.append(method.getName());
    sb.append("(");

    Class[] types = method.getParameterTypes();
    for (int i = 0; i < types.length; ++i) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append(types[i].getName());
    }

    sb.append(")");
    return sb.toString();
  }
}
