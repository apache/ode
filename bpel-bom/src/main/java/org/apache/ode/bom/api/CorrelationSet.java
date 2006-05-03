/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.api;

import javax.xml.namespace.QName;

/**
 * BPEL correlation set declaration. A correlation set is--like a variable--declared in
 * a scope-like construct (see {@link Scope}.
 */
public interface CorrelationSet extends BpelObject {

  /**
   * Get the scope in which this correlation set is declared.
   *
   * @return declaring scope
   */
  Scope getDeclaringScope();

  /**
   * Get the name of this correlation set.
   *
   * @return correlation set name
   */
  String getName();


  /**
   * Set the name of this correlation set.
   * @param name correlation set name
   */
  void setName(String name);

  /**
   * Get the (ordered) set of properties that define this correlation set.
   * Properties are returned by their qualified name.
   * @return set of defining properties
   */
  QName[] getProperties();

  /**
   * Set the (ordered) set of properties that define this correlation set.
   * @param properties set of defining properties
   */
  void setProperties(QName[] properties);

}
