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

import java.io.Serializable;

import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.runtime.channels.ParentScopeChannel;
import org.apache.ode.bpel.runtime.channels.TerminationChannel;

class ActivityInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    /** Activity instance identifier */
    long aId;

    /** Activity definition. */
    OActivity o;
    TerminationChannel self;
    ParentScopeChannel parent;

    ActivityInfo(long aid, OActivity o, TerminationChannel self, ParentScopeChannel parent) {
        assert o != null;
        assert self != null;
        assert parent != null;

        this.o = o;
        this.self = self;
        this.parent = parent;
        this.aId = aid;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("(");
        buf.append(o);
        buf.append(',');
        buf.append(self);
        buf.append(',');
        buf.append(parent);
        buf.append(')');
        return buf.toString();
    }

    public int hashCode() {
        return (int)aId;
    }

}
