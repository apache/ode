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

import java.net.URI;

/**
 * BPEL Object Model representation of a BPEL &lt;import&gt;.
 * The &lt;import&gt; element is used within a BPEL4WS process to explicitly
 * indicate a dependency on external XML Schema or WSDL definitions.
 * Any number of &lt;import&gt; elements may appear as children of the &lt;process&gt; element.
 * Each &lt;import&gt; element contains three mandatory attributes:
 * <ol>
 * <li><code>namespace<code> - specifies the URI namespace of the imported definitions </li>
 * <li><code>location</code> - contains a URI indicating the location of a document that contains
 *                             relevant definitions in the namespace specified</li>
 * <li><code>importType</code> - identifies the type of document being imported by providing the URI
 *                               of the encoding language. The value MUST be set to
 *                               "http://www.w3.org/2001/XMLSchema" when importing an XML Schema
 *                               1.0 documents, and to "http://schemas.xmlsoap.org/wsdl/" when importing
 *                               WSDL 1.1 documents. </li>
 */
public interface Import extends BpelObject {

  /** Value of <code>importType</code> for XML Schema 1.0 */
  public static final String IMPORTTYPE_XMLSCHEMA10 = "http://www.w3.org/2001/XMLSchema";

  /** Value of <code>importType</code> for WSDL 1.1 */
  public static final String IMPORTTYPE_WSDL11 = "http://schemas.xmlsoap.org/wsdl/";


  /**
   * Get the namesapce of the imported definitions.
   * @todo change String to URI
   * @return namespace URI
   */
  public String getNamespace();

  /**
   * Set the namespace of the imported definitions.
   * @todo change String to URI
   * @param namespace namespace URI
   */
  public void setNamespace(String namespace);

  /**
   * Get the location URI of the imported resource.
   * @return location URI of imported resource
   */
  public URI getLocation();

  /**
   * Set the location URI of the imported resource.
   * @param location location URI of imported resource
   */
  public void setLocation(URI location);

  /**
   * Get the encoding language.
   * @todo change String to URI
   * @return URI of the encoding language
   */
  public String getImportType();

  /**
   * Set the encoding language.
   * @todo change String to URI
   * @param importType URI of the encoding language
   */
  public void setImportType(String importType);

}
