/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.utils.xsd;

import javax.xml.namespace.QName;

public interface SchemaModel {
  /**
   * <p>
   * Determines if two types are compatible using the following logic:
   * </p>
   * 
   * <ul>
   * <li>
   * if type a is Element, and type b is Element, they must be identical
   * element types.
   * </li>
   * <li>
   * if type a and/or b is  element/schema, schema/element, or schema/schema,
   * they  are compatible if one is derived from the other
   * </li>
   * </ul>
   * 
   *
   * @return
   */
  public boolean isCompatible(QName type1, QName type2);

  /**
   * Checks if type is a simple type.
   *
   * @return DOCUMENTME
   */
  public boolean isSimpleType(QName type);

  /**
   * Checks if model knows the type as an element type.
   *
   * @return DOCUMENTME
   */
  public boolean knowsElementType(QName elementType);

  /**
   * Checks if the model know the type as a schema type.
   *
   * @return DOCUMENTME
   */
  public boolean knowsSchemaType(QName schemaType);
}
