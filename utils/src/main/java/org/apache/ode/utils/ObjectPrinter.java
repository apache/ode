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
//        buf.append(CollectionUtils.makeCollection(ArrayList.class, (Object[]) objects[i]));
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
    buf.append(thiz.getClass().getSimpleName());
    buf.append(' ');
    buf.append(stringifyNvList(objects));
    buf.append('}');

    return buf.toString();
  }
}
