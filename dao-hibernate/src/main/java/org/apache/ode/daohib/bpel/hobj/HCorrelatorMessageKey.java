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
package org.apache.ode.daohib.bpel.hobj;


/**
 * Hibernate table for representing the pre-computed keys for a message
 * targetted at the BPEL process with no matching instance at the time of
 * receipt (and createInstance is not possible).
 *
 * @hibernate.class table="BPEL_CORRELATOR_MESSAGE_CKEY"
 */
public class HCorrelatorMessageKey extends HObject {
  /** Correlation Key canonical string representation. */
  private String _keyCanonical;

  private HCorrelatorMessage _owner;

  /** Constructor. */
  public HCorrelatorMessageKey() {
    super();
  }

  /**
   * Canonical string representation of the correlation key.
   *
   * @hibernate.property
   *   column="CKEY"
   *   not-null="true"
   * @hibernate.column
   *   name="CKEY"
   *   index="IDX_BPEL_CORRELATOR_MESSAGE_CKEY"
   */
  public String getCanonical() {
    return _keyCanonical;
  }

  /** @see #getCanonical()  */
  public void setCanonical(String canonical) {
    _keyCanonical = canonical;
  }

  /**
   * The message with which this correlation key value is associated.
   * @hibernate.many-to-one
   *   column="CORRELATOR_MESSAGE_ID" foreign-key="none"
   */
  public HCorrelatorMessage getOwner() {
    return _owner;
  }

  public void setOwner(HCorrelatorMessage owner) {
    _owner = owner;
  }
}
