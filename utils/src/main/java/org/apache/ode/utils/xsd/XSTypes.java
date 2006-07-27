package org.apache.ode.utils.xsd;

import javax.xml.namespace.QName;

/**
 * Analyzes schema types and provides facilities to convert them
 * to java types.
 */
public class XSTypes {

  public static Object toJavaObject(QName type, String value) {
    String foundType = null;
    try {
      if (isDecimal(type)) {
        foundType = "number";
        return Long.valueOf(value);
      } else if (isFloat(type)) {
        foundType = "double";
        return Double.valueOf(value);
      } else if (isBoolean(type)) {
        foundType = "boolean";
        return Boolean.valueOf(value);
      } else if (isFloat(type)) {
        foundType = "double";
        return Double.valueOf(value);
      } else if (isDate(type)) {
        foundType = "date";
        return new XMLCalendar(value);
      } else if (isString(type)) {
        foundType = "string";
        return value;
      }
    } catch (Exception nfe) {
      throw new IllegalArgumentException("The type " + foundType + " has been detected using the XSD type " +
              type + " but the parsing failed! The provided value is probably not of the right type:" + nfe.toString());
    }
    throw new IllegalArgumentException("Couldn't find java type for " + type);
  }

  public static boolean isNumber(QName type) {
    try {
      DECIMAL.valueOf(type.getLocalPart().toUpperCase());
      FLOAT.valueOf(type.getLocalPart().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isDate(QName type) {
    try {
      STRING.valueOf(type.getLocalPart().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isString(QName type) {
    try {
      STRING.valueOf(type.getLocalPart().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isDecimal(QName type) {
    try {
      DECIMAL.valueOf(type.getLocalPart().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isFloat(QName type) {
    try {
      FLOAT.valueOf(type.getLocalPart().toUpperCase());
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isBoolean(QName type) {
    return "boolean".equals(type.getLocalPart());
  }

  enum DECIMAL {
    DECIMAL, INTEGER, NONPOSITIVEINTEGER, LONG, NONNEGATIVEINTEGER, NEGATIVEINTEGER, INT, UNSIGNEDLONG,
    POSITIVEINTEGER, SHORT, BYTE, UNSIGNEDINT, UNSIGNEDSHORT, UNSIGNEDBYTE
  }

  enum FLOAT {
    FLOAT, DOUBLE
  }

  enum STRING {
    STRING, NORMALIZEDSTRING, TOKEN
  }

  enum DATEBASE {
    DATETIME, DATE
  }

}
