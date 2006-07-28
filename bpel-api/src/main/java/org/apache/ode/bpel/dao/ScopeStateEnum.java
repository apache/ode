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
package org.apache.ode.bpel.dao;

public class ScopeStateEnum {
  /** DOCUMENTME */
  public static final ScopeStateEnum ACTIVE = new ScopeStateEnum("ACTIVE");

  /** DOCUMENTME */
  public static final ScopeStateEnum FAULTED = new ScopeStateEnum("FAULTED");

  /** DOCUMENTME */
  public static final ScopeStateEnum FAULTHANDLER = new ScopeStateEnum("FAULTHANDLER");

  /** DOCUMENTME */
  public static final ScopeStateEnum COMPLETED = new ScopeStateEnum("COMPLETED");

  /** DOCUMENTME */
  public static final ScopeStateEnum COMPENSATING = new ScopeStateEnum("COMPENSATING");

  /** DOCUMENTME */
  public static final ScopeStateEnum COMPENSATED = new ScopeStateEnum("COMPENSATED");
  private final String myName; // for debug only

  public ScopeStateEnum(String name) {
    myName = name;
  }

  public boolean equals(Object o) {
    return ((ScopeStateEnum)o).myName.equals(myName);
  }

  public int hashCode() {
    return myName.hashCode();
  }

  public String toString() {
    return myName;
  }
}
