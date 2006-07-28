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

/**
 * Representaiton of a BPEL <code>&lt;correlation&gt;</code> modifier. A correlation
 * is a tuple consisting of a communication element (such as an invoke, receive, or onMessage)
 * a correlation set, and an initiate flag
 */
public interface Correlation extends BpelObject {
  /**
   * Initiate correlation set.
   */
  public short INITIATE_YES = 1;

  /**
   * Do not initiate correlation set (correlation is a constraint).
   */
  public short INITIATE_NO = -1;

  /**
   * Possibly initiate correlation set.
   */
  public short INITIATE_RENDEZVOUS = 0;


  /**
   * Input correlation pattern (pattern="in").
   */
  public static final short CORRPATTERN_IN = 0x01;

  /**
   * Output correlation pattern (pattern="out").
   */
  public static final short CORRPATTERN_OUT = 0x02;

  /**
   * In-out correlation pattern (pattern="inout")
   */
  public static final short CORRPATTERN_INOUT = CORRPATTERN_IN | CORRPATTERN_OUT;

  /**
   * Get the name of the referenced correlation set.
   *
   * @return correlation set
   */
  String getCorrelationSet();

  /**
   * Set the referenced correlation set.
   *
   * @param correlationSetName
   */
  void setCorrelationSet(String correlationSetName);

  /**
   * Get the value of the initiate flag.
   *
   * @return one of <code>{@link Correlation}.INITATE_XXX</code> constants
   */
  short getInitiate();

  /**
   * Set the value of the initiate flag.
   *
   * @param initiate one of <code>{@link Correlation}.INITATE_XXX</code> constants
   */
  void setInitiate(short initiate);

  /**
   * Get the correlation pattern.
   * @return the correlation pattern, one of:
   * <ul>
   * <li>{@link #CORRPATTERN_IN}</li>
   * <li>{@link #CORRPATTERN_OUT}</li>
   * <li>{@link #CORRPATTERN_INOUT}</li>
   * </ul>
   */
  short getPattern();

  /**
   * Set the correlation pattern.
   *
   * @param pattern one of:
   * <ul>
   * <li>{@link #CORRPATTERN_IN}</li>
   * <li>{@link #CORRPATTERN_OUT}</li>
   * <li>{@link #CORRPATTERN_INOUT}</li>
   * </ul>
   */
   void setPattern(short pattern);
}
