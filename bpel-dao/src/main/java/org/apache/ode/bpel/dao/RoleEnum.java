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

import java.util.ArrayList;
import java.util.List;

/**
 * Partner-link role enumeration; one of {@link #MY_ROLE} or {@link
 * #PARTNER_ROLE}.
 */
public class RoleEnum {

  /** My Role */
  public static final RoleEnum MY_ROLE = new RoleEnum("MY_ROLE");

  /** Partner Role */
  public static final RoleEnum PARTNER_ROLE = new RoleEnum("PARTNER_ROLE");

  private static final List<RoleEnum> ENUMS = new ArrayList<RoleEnum>();
  private short _id;
  private transient String _name;

  private RoleEnum(String name) {
    _id = (short)ENUMS.size();
    _name = name;
    ENUMS.add(this);
  }

  /**
   * @see Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    return ((RoleEnum)o)._id == _id;
  }

  /**
   * DOCUMENTME
   * 
   * @param code
   *          DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public static RoleEnum fromCode(short code) {
    return ENUMS.get(code);
  }

  /**
   * @see Object#hashCode()
   */
  public int hashCode() {
    return _id;
  }

  /**
   * DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public short toCode() {
    return _id;
  }

  /**
   * @see Object#toString()
   */
  public String toString() {
    return "RoleEnum." + _name;
  }

}
