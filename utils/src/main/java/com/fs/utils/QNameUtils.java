/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.utils;

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
