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
 * Used to store large data sets into a single table. When an HObject
 * instance needs to store as part of its state large binary or text
 * data, a reference to an instance of this class must be created.
 * @hibernate.class table="LARGE_DATA"
 */
public class HLargeData extends HObject {

  private byte[] binary = null;

  public HLargeData() {
    super();
  }

  public HLargeData(byte[] binary) {
    super();
    this.binary = binary;
  }

  public HLargeData(String text) {
    super();
    this.binary = text.getBytes();
  }

  /**
   * @hibernate.property type="blob" length="2147483600" column="BIN_DATA"
   */
  public byte[] getBinary() {
    return binary;
  }

  public void setBinary(byte[] binary) {
    this.binary = binary;
  }

  public String getText() {
    return new String(binary);
  }
}
