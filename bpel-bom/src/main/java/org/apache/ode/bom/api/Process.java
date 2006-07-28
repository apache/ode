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

import java.util.Set;

/**
 * BPEL Object Model (BOM) representation of a BPEL process.
 */
public interface Process extends Scope, JoinFailureSuppressor {
  /**
   * BPEL Version 1.1 constant.
   */
  public static final short BPEL_V110 = 110;

  /**
   * BPEL Version 2.0 constant.
   */
  public static final short BPEL_V200 = 200;

  /**
   * Set the name of the process.
   *
   * @param name name of the process
   */
  void setName(String name);

  /**
   * Get the name of the process.
   *
   * @return name of the process
   */
  String getName();

  /**
   * Get the BPEL version of this process.
   * @return one of: {@link #BPEL_V110}, {@link #BPEL_V200}
   */
  short getBpelVersion();


  /**
   * Set the BPEL version for this process.
   * @param bpelVersion one of: {@link #BPEL_V110}, {@link #BPEL_V200}
   */
  void setBpelVersion(short bpelVersion);

  /**
   * Set the process-level activity.
   *
   * @param root process-level activity
   */
  void setRootActivity(Activity root);

  /**
   * Get the root, process-level activity.
   *
   * @return root process-level activity
   */
  Activity getRootActivity();

  /**
   * Set the source URL of the BPEL source document as a String.
   *
   * @param source URL of BPEL source document
   */
  void setSource(String source);

  /**
   * Get the URL of the BPEL source document as a String.
   *
   * @return BPEL source URL.
   */
  String getSource();

  /**
   * Get the process' target namespace.
   *
   * @return process' target namespace
   */
  String getTargetNamespace();

  /**
   * Set the process' target namespace.
   * @param uri the process' target namespace.
   */
  void setTargetNamespace(String uri);
  
  
  /**
   * Get the default query language.
   * @return the default query language.
   */
  String getQueryLanguage();
  
  /**
   * Set the default query language.
   * @param queryLanguage process-wide default query language
   */
  void setQueryLanguage(String queryLanguage);

  /**
   * Get the default expression language.
   * @return default expression language
   */
  String getExpressionLanguage();

  /**
   * Set the default expression language.
   * @param expLanguage default expression language
   */
  void setExpressionLanguage(String expLanguage);

  /**
   * Get the <code>&lt;import&gt;</code>(s) of the process.
   * @return {@link Set} of {@link Import}s
   */
  Set<Import> getImports();

  /**
   * Add an <code>&lt;import&gt;</code>(s) to the process' set of imports.
   * @param imprt {@link Import} to add
   */
  void addImport(Import imprt);


  /**
   * Remove an <code>&lt;import&gt;</code>(s) from the process' set of imports.
   * @param imprt {@link Import} to remove
   */
  void removeImport(Import imprt);

}
