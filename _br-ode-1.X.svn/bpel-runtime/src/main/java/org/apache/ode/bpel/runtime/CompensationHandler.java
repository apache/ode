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
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.runtime.channels.CompensationChannel;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;


/**
 * A handle to a compensation handler.
 */
public class CompensationHandler implements Serializable, Comparable<CompensationHandler> {
	private static final long serialVersionUID = 1L;

	/** The scope to which this compensation handler belongs. */
  final ScopeFrame compensated;

  /** Compensation activation channel. */
  final CompensationChannel compChannel;

  /** Time that the scope was started. */
  final long scopeStartTime;

  /** Time that the scope was completed. */
  final long scopeEndTime;

  CompensationHandler(ScopeFrame compensated, CompensationChannel compChannel, long scopeStartTime, long scopeEndTime) {
  	assert compChannel != null;
  	
    this.compensated = compensated;
    this.compChannel = compChannel;
    this.scopeEndTime = scopeEndTime;
    this.scopeStartTime = scopeStartTime;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer("{CompensationHandler ch=");
    buf.append(compChannel);
    buf.append(", scope=");
    buf.append(compensated);
    buf.append("}");
    return buf.toString();
  }

  static Set<CompensationHandler> emptySet() {
    return Collections.emptySet();
  }

  public int compareTo(CompensationHandler that) {
	return (int) (that.scopeStartTime - this.scopeEndTime);
  }

}
