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

import javax.xml.namespace.QName;

/**
 * Utility methods for dealing with XML qualified names ({@link QName}s).
 */
public class QNameUtils {
  private QNameUtils() {
  }

  /**
   * Create a string representation from a {@link QName}. The string will have the form
   * <code>{URI}localpart</code>.
   *
   * @param qname a {@link QName}.
   *
   * @return stringified representation
   */
  public static String fromQName(QName qname) {
    return "{" + qname.getNamespaceURI() + "}" + qname.getLocalPart();
  }

  /**
   * Create a {@link QName} object from its stringified representation.
   *
   * @see #fromQName(javax.xml.namespace.QName)
   * @param s stringified representation
   * @return de-stringified {@link QName}
   * @throws IllegalArgumentException if the given string is not a valid stringified {@link QName}.
   */
  public static QName toQName(String s) {
    if (s.charAt(0) != '{') {
      throw new IllegalArgumentException("Malformed QNAME: " + s);
    }

    int idx = s.lastIndexOf('}');

    if (idx == 0) {
      throw new IllegalArgumentException("Malformed QNAME: " + s);
    }

    return new QName(s.substring(1, idx), s.substring(idx + 1, s.length()));
  }
}
