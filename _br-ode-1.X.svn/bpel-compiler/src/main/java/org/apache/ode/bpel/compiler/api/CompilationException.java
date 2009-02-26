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
package org.apache.ode.bpel.compiler.api;

/**
 * Exception indicating a compilation error. 
 */
public class CompilationException extends RuntimeException {
  private static final long serialVersionUID = -4683674811787611083L;
  private CompilationMessage _msg;

  public CompilationException(CompilationMessage msg, Throwable cause) {
    super(msg.toErrorString(),cause);
    _msg = msg;
  }
  /**
   * @see Exception#Exception(String,Throwable)
   */
  public CompilationException(CompilationMessage msg) {
    this(msg, null);
  }

  public String toErrorMessage() {
    return _msg.toErrorString();
  }

  /** Get the {@link CompilationMessage} associated with this exception}. */
  public CompilationMessage getCompilationMessage() {
    return _msg;
  }
}
