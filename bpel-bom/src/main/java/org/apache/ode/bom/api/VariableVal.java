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


/**
 * Assignment L/R-value defined by a location within a BPEL
 * variable.
 */
public interface VariableVal extends From, To {
  /**
   * Get the name of the variable.
   *
   * @return variable name
   */
  String getVariable();

  /**
   * Set the name of the varName.
   *
   * @param varName varName name
   */
  void setVariable(String varName);

  /**
   * Get the (optional) message part.
   *
   * @return name of the message part, or <code>null</code>
   */
  String getPart();

  /**
   * Set the (optional) message part.
   *
   * @param part name of the message part, or <code>null</code>
   */
  void setPart(String part);

  /**
   * Get the (optional) location query.
   *
   * @return location query, or <code>null</code>
   */
  Query getLocation();

  /**
   * Set the (optional) location query.
   *
   * @param location location query, or <code>null</code>
   */
  void setLocation(Query location);
}
