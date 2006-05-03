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

import org.apache.ode.utils.NSContext;

/**
 * Common interface to all BPEL object model (BOM) objects. Provides for location
 * information (i.e. line numbers) and namespace context (XML namespace prefix maps).
 */
public interface BpelObject {

  /**
   * Get the line number in the BPEL source document where this object is defined.
   *
   * @return line number
   */
  int getLineNo();

  /**
   * Set the line number in the BPEL source document where this object is defined.
   *
   * @param lineNo line number
   */
  void setLineNo(int lineNo);

  /**
   * Get the namespace context for this BPEL object (i.e. prefix-to-namespace mapping).
   *
   * @return namespace context
   */
  NSContext getNamespaceContext();

  /**
   * Set the namespace context for this BPEL object (i.e. prefix-to-namespace mapping).
   *
   * @param ctx namespace context
   */
  void setNamespaceContext(NSContext ctx);

  /**
   * Returns the human-readable description of this object.
   * 
   * @return the description
   */
  String getDescription();
  
  /**
   * Set a description of this model element.
   * @param description human-readable description
   */
  void setDescription(String description);

}
