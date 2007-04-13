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
package org.apache.ode.bpel.explang;

/**
 * A {@link EvaluationException} indicating a type conversion error.
 */
public class TypeCastException extends EvaluationException {
  public static final short TYPE_STRING = 0;
  public static final short TYPE_DATE = 1;
  public static final short TYPE_DURATION = 2;
  public static final short TYPE_NODELIST = 3;
  public static final short TYPE_BOOLEAN = 4;
  public static final short TYPE_NODE = 5;
  public static final short TYPE_NUMBER = 6;

  private short _type;
  private String _val;

  public TypeCastException(short type, String val) {
    super("Type conversion error from: " + val,null);
    _type = type;
    _val = val;
  }

  public short getToType() {
    return _type;
  }

  public String getVal() {
    return _val;
  }

}
