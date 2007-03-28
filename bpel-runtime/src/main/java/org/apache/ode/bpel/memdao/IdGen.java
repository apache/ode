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
package org.apache.ode.bpel.memdao;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique keys for process instances and scopes.
 * 
 */
class IdGen {
  private static AtomicLong PROC_ID = new AtomicLong(0);
  private static AtomicLong SCOPE_ID = new AtomicLong(0);
  private static AtomicLong CSET_ID = new AtomicLong(0);

  public static Long newProcessId() {
    return PROC_ID.getAndIncrement();
  }

  public static Long newScopeId() {
    return SCOPE_ID.getAndIncrement();
  }

  public static Long newCorrelationSetId() {
    return CSET_ID.getAndIncrement();
  }

}
