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

/**
 * UUIDGen adopted from the juddi project
 * (http://sourceforge.net/projects/juddi/)
 */
package org.apache.ode.utils.uuid;

import org.apache.ode.utils.GUID;

/**
 * Used to create new universally unique identifiers or UUID's (sometimes
 * called GUID's).  UDDI UUID's are allways formmated according to DCE UUID
 * conventions.
 */
public class UUIDGen {

  /**
   * Creates a new UUID. The algorithm used is the one in {@link UUID}, the
   * open group algorithm took too damn long.
   *
   * @return a new "globally" unique identifier
   */
  public String nextUUID() {
    return new GUID().toString();
  }

}
