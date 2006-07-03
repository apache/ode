
/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package com.fs.pxe.sfwk.impl.xmlschema;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;


/**
 * Utitlity class for reasoning about schema types.
 */
public class SchemaChecker {
  static boolean init = false;
  static Map<QName, Short> _typeMap = new HashMap<QName, Short>();

  public SchemaChecker() {
    if (!init) {
      try {
        Field[] fields = SchemaConstants.class.getFields();

        for (int i = 0; i < fields.length; ++i) {
          if (!fields[i].getName().startsWith("XSD")) {
            continue;
          }

          QName qname = (QName)fields[i].get(null);
          short s = fields[++i].getShort(null);
          _typeMap.put(qname, Short.valueOf(s));
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * DOCUMENTME
   *
   * @param schemaType DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public boolean isBinary(QName schemaType) {
    // TODO: Change this to check for derived types
    return schemaType.equals(SchemaConstants.XSD_HEXBINARY)
           || schemaType.equals(SchemaConstants.XSD_BASE64BINARY);
  }

  /**
   * Is the given schema type complex?
   *
   * @param schemaType
   *
   * @return <code>true</code> if the type is complex, <code>false</code>
   *         otherwise
   */
  public boolean isComplex(QName schemaType) {
    short s = getTypeForPrimitive(schemaType);

    return s == -1;
  }

  /**
   * Is the given schema type some sort of a number?
   *
   * @param schemaType
   *
   * @return
   */
  public boolean isNumber(QName schemaType) {
    short s = getTypeForPrimitive(schemaType);

    switch (s) {
    case SchemaConstants.XSD_FLOAT_CONST:
    case SchemaConstants.XSD_DOUBLE_CONST:
    case SchemaConstants.XSD_DECIMAL_CONST:
    case SchemaConstants.XSD_INTEGER_CONST:
    case SchemaConstants.XSD_INT_CONST:
    case SchemaConstants.XSD_SHORT_CONST:
    case SchemaConstants.XSD_LONG_CONST:
      return true;
    }

    return false;
  }

  /**
   * Is the given schema type simple?
   *
   * @param schemaType
   *
   * @return <code>true</code> if the type is simple, <code>false</code>
   *         otherwise
   */
  public boolean isSimple(QName schemaType) {
    return !isComplex(schemaType);
  }

  /**
   * DOCUMENTME
   *
   * @param schemaType DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public boolean isText(QName schemaType) {
    // TODO: Change this to check for derived types
    return schemaType.equals(SchemaConstants.XSD_STRING);
  }

  /**
   * DOCUMENTME
   *
   * @param str DOCUMENTME
   * @param schemaType DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public Object fromCanonicalForm(String str, QName schemaType) {
    if (str == null) {
      return null;
    }

    short type = getTypeForPrimitive(schemaType);

    if (type == -1) {
      throw new IllegalArgumentException("Unsupported primitive type "
                                         + schemaType);
    }

    return CanonicalForm.fromCanonicalForm(str, type);
  }

  /**
   * DOCUMENTME
   *
   * @param obj DOCUMENTME
   * @param schemaType DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public String toCanonicalForm(Object obj, QName schemaType) {
    if (obj == null) {
      return null;
    }

    short type = getTypeForPrimitive(schemaType);

    if (type == -1) {
      throw new IllegalArgumentException("Unsupported primitive type "
                                         + schemaType);
    }

    return CanonicalForm.toCanonicalForm(obj, type);
  }

  static short getTypeForPrimitive(QName qname) {
    Short s = _typeMap.get(qname);

    return (s == null)
           ? (-1)
           : s.shortValue();
  }

}
