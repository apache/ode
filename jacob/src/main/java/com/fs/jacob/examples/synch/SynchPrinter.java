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
package com.fs.jacob.examples.synch;

import com.fs.jacob.Abstraction;
import com.fs.jacob.SynchChannel;
import com.fs.jacob.SynchML;
import com.fs.jacob.vpu.FastSoupImpl;
import com.fs.jacob.vpu.JacobVPU;

/**
 * DOCUMENTME.
 * <p>Created on Mar 4, 2004 at 4:22:05 PM.</p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class SynchPrinter  {

  public static final class SystemPrinter extends Abstraction {
    private SynchPrintChannel self;
    public SystemPrinter(SynchPrintChannel self) {
      this.self = self;
    }
    public void self() {
      object(true, new SynchPrintML(self) {
        public SynchChannel print(String msg) {
          System.out.println(msg);
          return null;
        }
      });
    }
  }

  public static final class Tester extends Abstraction {
    public void self() {
      final SynchPrintChannel p = newChannel(SynchPrintChannel.class);
      instance(new SystemPrinter(p));
      object(new SynchML(p.print("1")) {
        public void ret() {
          object(new SynchML(p.print("2")) {
            public void ret() {
              object(new SynchML(p.print("3")) {
                public void ret() {
                }
              });
            }
          });
        }
      });
    }
  }

  public static void main(String args[]) {
    JacobVPU vpu = new JacobVPU();
    vpu.setContext(new FastSoupImpl(null));
    vpu.inject(new Tester());
    while (vpu.execute()) {
      // run
    }
  }

}
