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
package org.apache.ode.jacob.vpu;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Statistics for the Jacob VPU.
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class Statistics {
  /** Number of VPU cycles. */
  public long numCycles;

  /** Total VPU run time (in ms). */
  public long totalRunTimeMs;

  /** Number of channels created. */
  public long channelsCreated;

  /** Number of messages sent. */
  public long messagesSent;

  /** Number of messsages received. */
  public long messagesRcvd;

  /** Number of continuations. */
  public long numContinuations;

  /** Total size of all _continuation (in bytes). */
  public long totalContinuationBytes;

  /** Number of enqueues to the run queue. */
  public long runQueueEntries;

  /** Total number of communication reductions. */
  public long numReductionsComm;

  /** Total number of structural reductions. */
  public long numReductionsStruct;

  /** Total time spent in client code (in ms). */
  public long totalClientTimeMs;

  /**
   * Total time spent in each {@link org.apache.ode.jacob.JavaMethodBody} method.
   */
  public final Map<String, PerTargetStatistics> byTarget = new HashMap<String, PerTargetStatistics>();

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public double getAvgClientTimeMs() {
    return (double)totalClientTimeMs / (double)numCycles;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public double getAvgContinuationSize() {
    return (double)totalContinuationBytes / (double)numContinuations;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public double getAvgKernelTimeMs() {
    return (double)getKernelTimeMs() / (double)numCycles;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public double getAvgRunTimeMs() {
    return (double)totalRunTimeMs / (double)numCycles;
  }

  /**
   * DOCUMENTME
   *
   * @return DOCUMENTME
   */
  public long getKernelTimeMs() {
    return totalRunTimeMs - totalClientTimeMs;
  }

  /**
   * DOCUMENTME
   *
   * @param target DOCUMENTME
   * @param runTimeMs DOCUMENTME
   */
  public void incRunTime(String target, long runTimeMs) {
    PerTargetStatistics pts = target(target);
    ++pts.invocations;
    pts.totalRunTimeMs += runTimeMs;
    pts.minRunTimeMs = Math.min(pts.minRunTimeMs, runTimeMs);
    pts.maxRunTimeMs = Math.max(pts.maxRunTimeMs, runTimeMs);
  }

  /**
   * Print the statistics to an output stream in english human-readable form.
   *
   * @param ps destination
   */
  public void printToStream(PrintStream ps) {
    ps.println("JACOB VPU Statistics:");

    Field[] fields = getClass().getFields();

    for (int i = 0; i < fields.length; ++i) {
      ps.print(fields[i].getName());
      ps.print(" = ");

      try {
        ps.println(fields[i].get(this));
      }
      catch (Exception ex) {
        ps.println(ex.toString());
      }
    }

    Method[] meth = getClass().getMethods();

    for (int i = 0; i < meth.length; ++i) {
      if (meth[i].getName().startsWith("get") && (meth[i].getParameterTypes().length == 0)) {
        ps.print(meth[i].getName().substring(3));
        ps.print(" = ");

        try {
          ps.println(meth[i].invoke(this, (Object[])null));
        }
        catch (Exception ex) {
          ps.println(ex.toString());
        }
      }
    }
  }

  private PerTargetStatistics target(String target) {
    PerTargetStatistics pts = byTarget.get(target);

    if (pts == null) {
      pts = new PerTargetStatistics();
      byTarget.put(target, pts);
    }

    return pts;
  }

  public static final class PerTargetStatistics {
    public long invocations;
    public long totalRunTimeMs;
    public long minRunTimeMs = Long.MAX_VALUE;
    public long maxRunTimeMs = Long.MIN_VALUE;

    public double getAvgRunTimePerInvocation() {
      return (double)totalRunTimeMs / (double)invocations;
    }

    public String toString() {
      return "(n=" + invocations + ", total=" + totalRunTimeMs + "ms, avg="
             + getAvgRunTimePerInvocation() + "ms, min=" + minRunTimeMs
             + "ms, max=" + maxRunTimeMs + "ms)";
    }
  }
}
