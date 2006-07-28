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
package org.apache.ode.bom.api;

import org.w3c.dom.Node;

/**
 * BOM representation of a BPEL expression language expression.
 */
public interface Expression extends BpelObject {

  /**
   * Get the expression language for this expression.
   * @return expression langauge URI or <code>null</code> if none specified
   */
  String getExpressionLanguage();

  /**
   * Set the expression "text" (i.e. the program listing).
   * @todo rename this method to setText()
   */
  void setXPathString(String xpathString);

  /**
   * Get the expression "text" (i.e. the program listing).
   * @todo rename this method to getText()
   * @return expression text
   */
  String getXPathString();
  
  /**
   * The expression as a DOM node.
   * @return
   */
  Node getNode();
  
  /**
   * Set the expression as a DOM node.
   * @param node
   */
  void setNode(Node node);

}
