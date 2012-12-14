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

import org.apache.ode.jacob.JacobRunnable;
import org.apache.ode.jacob.Val;

/**
 * Cell process template Java representation. This class is equivalent to the
 * following process calculus expression:
 * <code>
 * Cell(self, val) = self ? [ read(r) = { Cell(self, val) | r ! val(val) } & write(newVal) = { Cell(self, newVal) } ]
 * </code>
 */
public class CELL_<T> extends JacobRunnable {
    private static final long serialVersionUID = 1550566086202728251L;

    private CellChannel _self;

    private T _val;

    public CELL_(CellChannel self, T val) {
        _self = self;
        _val = val;
    }

    public void run() {
        // INSTANTIATION{Cell(run,val)}
        // ==> run ? [ read(r)={...} & write(newVal)={...} ]
        object(new CellChannelListener(_self) {
            private static final long serialVersionUID = 8883128084307471572L;

            public void read(Val r) {
                // COMMUNICATION{x & [read... & ... ] | x ! read} ==> Cell(run, val) ...
                instance(new CELL_<T>(_self, _val));

                // ... | r ! val(val)
                r.val(_val);

                // Note: sequential Java above translates to parallel proc calc expression!
            }

            @SuppressWarnings("unchecked")
            public void write(Object newVal) {
                // COMMUNICATION{x & [... & write...] ==> Cell(run, newVal)
                instance(new CELL_(_self, newVal));
            }
        });
    }

    public String toString() {
        return "CellProcess[self=" + _self + ", val=" + _val + "]";
    }
}
