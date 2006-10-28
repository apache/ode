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
package org.apache.ode.jacob;

public abstract class Sequence extends JacobRunnable {
  private int _size;
  private int _step;
  private SynchChannel _done;

  public Sequence(int size, SynchChannel done) {
    this(size, 0, done);
  }

  private Sequence(int size, int step, SynchChannel done) {
    _size = size;
    _step = step;
    _done = done;
  }

  /**
   * DOCUMENTME
   */
  public void run() {
    if (_step >= _size) {
      if (_done != null) {
        _done.ret();
      }
    } else {
      SynchChannel r = newChannel(SynchChannel.class);
      object(new SynchChannelListener(r) {
          private static final long serialVersionUID = -6999108928780639603L;

          public void ret() {
            ++_step;
            instance(Sequence.this);
          }
        });
      instance(reduce(_step, r));
    }
  }

  protected abstract JacobRunnable reduce(int n, SynchChannel r);
}
