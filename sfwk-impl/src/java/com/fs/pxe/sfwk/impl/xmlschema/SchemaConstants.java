/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */



package com.fs.pxe.sfwk.impl.xmlschema;

import javax.xml.namespace.QName;


/**
 * Constants related to XML-schema types.
 */
public interface SchemaConstants {
  /** Namespace for XML schemas */
  public static final String NS_XMLSCHEMA_2001 = "http://www.w3.org/2001/XMLSchema";

  /** The unrestricted XML schema string: <code>xsd:string</code> */
  public static final QName XSD_STRING = new QName(NS_XMLSCHEMA_2001, "string");
  static final short XSD_STRING_CONST = 1;

  /** DOCUMENTME */
  public static final QName XSD_BOOLEAN = new QName(NS_XMLSCHEMA_2001,
                                                    "boolean");
  static final short XSD_BOOLEAN_CONST = 2;

  /** The unrestricted XML schema integer: <code>xsd:int</code> */
  public static final QName XSD_INTEGER = new QName(NS_XMLSCHEMA_2001,
                                                    "integer");
  static final short XSD_INTEGER_CONST = 3;

  /** DOCUMENTME */
  public static final QName XSD_INT = new QName(NS_XMLSCHEMA_2001, "int");
  static final short XSD_INT_CONST = 4;

  /** DOCUMENTME */
  public static final QName XSD_SHORT = new QName(NS_XMLSCHEMA_2001, "int");
  static final short XSD_SHORT_CONST = 5;

  /** DOCUMENTME */
  public static final QName XSD_FLOAT = new QName(NS_XMLSCHEMA_2001, "float");
  static final short XSD_FLOAT_CONST = 6;

  /** DOCUMENTME */
  public static final QName XSD_DOUBLE = new QName(NS_XMLSCHEMA_2001, "double");
  static final short XSD_DOUBLE_CONST = 7;

  /** DOCUMENTME */
  public static final QName XSD_DECIMAL = new QName(NS_XMLSCHEMA_2001,
                                                    "decimal");
  static final short XSD_DECIMAL_CONST = 8;

  /** DOCUMENTME */
  public static final QName XSD_DURATION = new QName(NS_XMLSCHEMA_2001,
                                                     "duration");
  static final short XSD_DURATION_CONST = 9;

  /** DOCUMENTME */
  public static final QName XSD_DATETIME = new QName(NS_XMLSCHEMA_2001,
                                                     "dateTime");
  static final short XSD_DATETIME_CONST = 10;

  /** DOCUMENTME */
  public static final QName XSD_DATE = new QName(NS_XMLSCHEMA_2001, "date");
  static final short XSD_DATE_CONST = 11;

  /** DOCUMENTME */
  public static final QName XSD_TIME = new QName(NS_XMLSCHEMA_2001, "time");
  static final short XSD_TIME_CONST = 12;

  /** DOCUMENTME */
  public static final QName XSD_GYEARMONTH = new QName(NS_XMLSCHEMA_2001,
                                                       "gYearMonth");
  static final short XSD_GYEARMONTH_CONST = 13;

  /** DOCUMENTME */
  public static final QName XSD_GYEAR = new QName(NS_XMLSCHEMA_2001, "gYear");
  static final short XSD_GYEAR_CONST = 14;

  /** DOCUMENTME */
  public static final QName XSD_GMONTHDAY = new QName(NS_XMLSCHEMA_2001,
                                                      "gMonthDay");
  static final short XSD_GMONTHDAY_CONST = 15;

  /** DOCUMENTME */
  public static final QName XSD_GDAY = new QName(NS_XMLSCHEMA_2001, "gDay");
  static final short XSD_GDAY_CONST = 16;

  /** DOCUMENTME */
  public static final QName XSD_HEXBINARY = new QName(NS_XMLSCHEMA_2001,
                                                      "hexBinary");
  static final short XSD_HEXBINARY_CONST = 17;

  /** DOCUMENTME */
  public static final QName XSD_BASE64BINARY = new QName(NS_XMLSCHEMA_2001,
                                                         "base64Binary");
  static final short XSD_BASE64BINARY_CONST = 18;

  /** DOCUMENTME */
  public static final QName XSD_QNAME = new QName(NS_XMLSCHEMA_2001, "QName");
  static final short XSD_QNAME_CONST = 19;

  /** DOCUMENTME */
  public static final QName XSD_LONG = new QName(NS_XMLSCHEMA_2001, "long");
  static final short XSD_LONG_CONST = 20;
}
