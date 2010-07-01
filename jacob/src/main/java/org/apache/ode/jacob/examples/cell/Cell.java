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
package org.apache.ode.jacob.examples.cell;

import org.apache.ode.jacob.Val;
import org.apache.ode.jacob.ap.ChannelType;


/**
 * Channel type for a cell. The channel allows reading of and setting the values of a cell.
 *
 * @jacob.kind
 */
@ChannelType
public interface Cell  {

  /**
   * Read the value of the cell.
   * @param replyTo channel to which the value of the cell is sent
   */
  public void read(Val replyTo);

  /**
   * Write the value of the cell.
   * @param newVal new value of the cell
   */
  public void write(Object newVal);
}
