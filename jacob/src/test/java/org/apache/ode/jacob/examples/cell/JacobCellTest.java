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

import junit.framework.TestCase;
import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.ValChannel;
import org.apache.ode.jacob.ValChannelListener;
import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
import org.apache.ode.jacob.vpu.JacobVPU;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JacobCellTest extends TestCase {
  private static Object _val;

  public JacobCellTest(String testName) {
    super(testName);
  }

  public void testJacobCell1() throws IOException {
    ExecutionQueueImpl fsoup = new ExecutionQueueImpl(null);
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

  static class CellTest1 extends JacobRunnable {
    public void run() {
      CellChannel cellChannel = newChannel(CellChannel.class, "cell");
      ValChannel retChannel = newChannel(ValChannel.class, "val");

      instance(new CELL_<String>(cellChannel, "foo"));
      object(new ValChannelListener(retChannel) {
          public void val(Object retVal) {
            _val = retVal;
          }
        });
      cellChannel.read(retChannel);
    }
  }

}
