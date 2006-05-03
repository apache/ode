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
 * BOM representation of a BPEL fault handler, i.e. a collection of catch blocks.
 */
public interface FaultHandler extends BpelObject {
  /**
   * Get the scope to which this fault handler belongs.
   *
   * @return scope owner scope
   */
  Scope getScope();

  /**
   * Gets the {@link Catch} blocks for this fault handler.
   *
   * @return array of {@link Catch} blocks
   */
  Catch[] getCatches();

  /**
   * Adds a {@link Catch} to the list of catch blocks.
   *
   * @param catchBlock catch block
   */
  void addCatch(Catch catchBlock);

}
