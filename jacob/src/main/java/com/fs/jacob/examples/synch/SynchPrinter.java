/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
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
