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

import java.util.List;

/**
 * BOM representation of the BPEL <code>&lt;assign&gt;</code> activity.
 * The <code>&lt;assign&gt;</code> activity is simply a collection of
 * <code>&lt;copy&gt;</code> entries ({@link Copy}).
 */
public interface AssignActivity extends Activity {

  /**
   * Get the list of <code>&lt;copy&gt;</code> entries for this activity.
   *
   * @return copy entries
   */
  List<Copy> getCopies();

  /**
   * Append a <code>&lt;copy&gt;</code> entry to the list of
   * copy <code>&lt;copy&gt;</code> entries.
   */
  void addCopy(Copy copy);

  /**
   * Add a <code>&lt;copy&gt;</code> entry to the list of
   * copy <code>&lt;copy&gt;</code> entries.
   *
   * @param idx  position of new entry (starting at 0)
   * @param copy new copy entry
   */
  void addCopy(int idx, Copy copy);


}
