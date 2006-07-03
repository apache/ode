/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.impl.xmlschema;

import java.util.Date;

import javax.xml.namespace.QName;


/**
 * CanonicalForm
 *
 * @author jguinney
 */
class CanonicalForm implements SchemaConstants {
  private CanonicalForm() {
  }

  static Object fromCanonicalForm(String s, short type) {
    switch (type) {
    case XSD_DURATION_CONST:
    case XSD_BASE64BINARY_CONST:
    case XSD_HEXBINARY_CONST:
    case XSD_STRING_CONST:
      return s;

    case XSD_BOOLEAN_CONST:
      return Boolean.valueOf(s);

    case XSD_FLOAT_CONST:
      return Float.valueOf(s);

    case XSD_LONG_CONST:
      return Long.valueOf(s);

    case XSD_DOUBLE_CONST:
    case XSD_DECIMAL_CONST:
      return Double.valueOf(s);

    case XSD_INTEGER_CONST:
    case XSD_INT_CONST:
      return Integer.valueOf(s);

    case XSD_SHORT_CONST:
      return Short.valueOf(s);

    case XSD_DATETIME_CONST:
    case XSD_DATE_CONST:
    case XSD_TIME_CONST:
    case XSD_GYEARMONTH_CONST:
    case XSD_GYEAR_CONST:
    case XSD_GMONTHDAY_CONST:
    case XSD_GDAY_CONST:
      return new Date(Long.parseLong(s));

    case XSD_QNAME_CONST:
      return qnameFromString(s);

    default:
      throw new IllegalArgumentException("unsupported type " + type);
    }
  }

  static QName qnameFromString(String s) {
    int idx = s.lastIndexOf('}');
    String uri = s.substring(1, idx);
    String local = s.substring(idx + 1, s.length());

    return new QName(uri, local);
  }

  static String toCanonicalForm(Object value, short type) {
    switch (type) {
    case XSD_STRING_CONST:
    case XSD_BOOLEAN_CONST:
    case XSD_FLOAT_CONST:
    case XSD_DOUBLE_CONST:
    case XSD_DECIMAL_CONST:
    case XSD_INTEGER_CONST:
    case XSD_INT_CONST:
    case XSD_SHORT_CONST:
    case XSD_LONG_CONST:
    case XSD_BASE64BINARY_CONST:
    case XSD_HEXBINARY_CONST:
    case XSD_DURATION_CONST:
      return value.toString();

    case XSD_DATETIME_CONST:
    case XSD_DATE_CONST:
    case XSD_TIME_CONST:
    case XSD_GYEARMONTH_CONST:
    case XSD_GYEAR_CONST:
    case XSD_GMONTHDAY_CONST:
    case XSD_GDAY_CONST:
      return Long.toString(((Date)value).getTime());

    case XSD_QNAME_CONST:

      QName name = (QName)value;

      return "{" + name.getNamespaceURI() + "}" + name.getLocalPart();

    default:
      throw new IllegalArgumentException("unsupported type " + type);
    }
  }
}
