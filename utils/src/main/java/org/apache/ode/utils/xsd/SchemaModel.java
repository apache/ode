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
