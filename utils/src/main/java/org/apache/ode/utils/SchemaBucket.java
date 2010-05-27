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
package org.apache.ode.utils;

import java.net.URL;


/**
 * Utility class for obtaining XML schemas for commonly used document types.
 */
public class SchemaBucket {

  /**
   * Get the Basic Profile 1.0 schema for WSDL, amended to include the errata.
   * @return a <code>URL</code> to the resource
   */
  public static URL getBp1_0WsdlSchema() {
    return SchemaBucket.class.getResource("wsdl.xsd");
  }

}
