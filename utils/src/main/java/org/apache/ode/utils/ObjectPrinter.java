/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils;


/**
 * Helper class for printing/formatting arbitrary objects.
 * 
 * <p>
 * Created on Feb 17, 2004 at 3:53:54 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class ObjectPrinter {
  /**
   * Get a the short (without package) class name.
   *
   * @param o some non-null object
   *
   * @return short class name of given object
   */
  public static String getShortClassName(Object o) {
    return getShortClassName(o.getClass());
  }

  /**
   * Get a the short (without package) class name.
   *
   * @param clazz some {@link Class} object
   *
   * @return short class name of given class
   */
  public static String getShortClassName(Class clazz) {
    String clsName = clazz.getName();
    String pkgName = clazz.getPackage()
                          .getName();

    if ((pkgName != null) && (pkgName.length() > 0)) {
      clsName = clsName.substring(pkgName.length() + 1);
    }

    return clsName;
  }

  /**
   * Generate a default trace message for method entry.
   *
   * @param methodName method name
   * @param args method arguments (in staggered name-value array format) (see
   *        {@link #stringifyNvList(java.lang.Object[])})
   *
   * @return default trace message
   */
  public static String stringifyMethodEnter(String methodName, Object[] args) {
    StringBuffer buf = new StringBuffer(">> ");
    buf.append(methodName);
    buf.append('(');
    buf.append(stringifyNvList(args));
    buf.append(')');

    return buf.toString();
  }

  /**
   * Stringify a staggered name-value list. Staggered NV-lists have the
   * following form:<code> Object nvlist[] = { "name1", val1, "name2", val2,
   * ... }</code>. The stringified representation looks like
   * <code>name1=val,name2=val2,...</code> where the values are obtained from
   * the {@link Object#toString()} method.
   *
   * @param objects staggered NV-list
   *
   * @return stringified representation
   */
  public static String stringifyNvList(Object[] objects) {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; objects != null && i < objects.length; ++i) {
      if (objects[i] == null)
        buf.append("<null>");
// 		NOTE: this causes blowups since array may be array of primitive type
// 				  which cannot be cast to 'Object[]'
//      else if (objects[i].getClass().isArray()){
//        buf.append(ArrayUtils.makeCollection(ArrayList.class, (Object[]) objects[i]));
      else
        buf.append(objects[i]);

      if ((i + 1) < objects.length) {
        if ((i % 2) == 0) {
          buf.append('=');
        } else {
          buf.append(',');
        }
      }
    }

    return buf.toString();
  }

  /**
   * A default <code>toString</code> implementation.
   *
   * @param thiz object for which to generate the string representation
   * @param objects a staggered name-value array.
   *
   * @return a concatenation of the object name and the name-value list
   */
  public static String toString(Object thiz, Object[] objects) {
    StringBuffer buf = new StringBuffer("{");
    buf.append(getShortClassName(thiz));
    buf.append(' ');
    buf.append(stringifyNvList(objects));
    buf.append('}');

    return buf.toString();
  }
}
