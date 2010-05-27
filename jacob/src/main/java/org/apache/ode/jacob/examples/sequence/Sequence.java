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
package org.apache.ode.jacob.examples.sequence;

import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.SynchChannel;
import org.apache.ode.jacob.SynchChannelListener;

/**
 * Abstract process that executes a number of steps sequentially.
 */
public abstract class Sequence extends JacobRunnable {
    private int _steps;
    private int _current;
    private SynchChannel _done;

    /**
     * Create a {@link Sequence} with a number of steps.
     *
     * @param steps number of steps
     * @param done synchronous callback
     */
    public Sequence(int steps, SynchChannel done) {
        _steps = steps;
        _current = 0;
        _done = done;
    }

    /**
     * Process execution block
     */
    public void run() {
        if (_current >= _steps) {
            if (_done != null) {
                _done.ret();
            }
        } else {
            SynchChannel r = newChannel(SynchChannel.class);
            object(new SynchChannelListener(r) {
                private static final long serialVersionUID = -6999108928780639603L;

                public void ret() {
                    ++_current;
                    instance(Sequence.this);
                }
            });
            instance(doStep(_current, r));
        }
    }

    /**
     * Execute a step
     * @param step step number
     * @param done notification after step completion
     * @return runnable process
     */
    protected abstract JacobRunnable doStep(int step, SynchChannel done);
}
