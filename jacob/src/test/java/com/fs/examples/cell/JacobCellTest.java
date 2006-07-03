/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.examples.cell;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import com.fs.jacob.Abstraction;
import com.fs.jacob.ValChannel;
import com.fs.jacob.ValML;
import com.fs.jacob.examples.cell.CELL_;
import com.fs.jacob.examples.cell.CellChannel;
import com.fs.jacob.vpu.FastSoupImpl;
import com.fs.jacob.vpu.JacobVPU;

public class JacobCellTest extends TestCase {
  private static Object _val;

  public JacobCellTest(String testName) {
    super(testName);
  }

  public void testJacobCell1() throws IOException {
    FastSoupImpl fsoup = new FastSoupImpl(null);
    JacobVPU vpu = new JacobVPU(fsoup, new CellTest1());


    while (vpu.execute()) {
      vpu.flush();
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      fsoup.write(bos);
      bos.close();
      System.err.println("CONTINUATION SIZE: " + bos.size());
    }
    vpu.dumpState();
    fsoup.dumpState(System.err);
    assertNotNull(_val);
    assertEquals("foo", _val);
  }

  static class CellTest1 extends Abstraction {
    public void self() {
      CellChannel cellChannel = newChannel(CellChannel.class, "cell");
      ValChannel retChannel = newChannel(ValChannel.class, "val");

      instance(new CELL_<String>(cellChannel, "foo"));
      object(new ValML(retChannel) {
          public void val(Object retVal) {
            _val = retVal;
          }
        });
      cellChannel.read(retChannel);
    }
  }

  private static class Compute extends Abstraction {
    ValChannel _out;
    int _x;

    public Compute(int x, ValChannel out) {
      _out = out;
      _x = x;
    }

    public void self() {
      int y = _x ^ _x;
      _out.val(Integer.valueOf(y));
    }
  }

  // TODO still needed?
  private static class Foo extends Abstraction {
    public void self() {
      ValChannel print = newChannel(ValChannel.class);
      instance(new Compute(1, print));
      instance(new Compute(2, print));
      instance(new Compute(2, print));
      instance(new Print(print));
    }
  }

  private static class Print extends Abstraction {
    ValChannel _val;

    public Print(ValChannel val) {
      _val = val;
    }

    public void self() {
      object(new ValML(_val) {
          public void val(Object retVal) {
            System.out.println(retVal);
          }
        });
    }
  }
}
