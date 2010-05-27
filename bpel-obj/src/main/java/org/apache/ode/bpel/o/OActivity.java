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
package org.apache.ode.bpel.o;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Compiled represnetation of a BPEL activity.
 */
public abstract class OActivity extends OAgent {
    static final long serialVersionUID = -1L  ;

    public OExpression joinCondition;
    public boolean suppressJoinFailure;
    public final Set<OLink>sourceLinks = new HashSet<OLink>();
    public final Set<OLink>targetLinks = new HashSet<OLink>();
    public String name;
    public OFailureHandling failureHandling;
    private OActivity parent;

    public String getType() {
        return getClass().getSimpleName();
    }

    public OActivity(OProcess owner, OActivity parent) {
        super(owner);
        this.parent = parent;
    }

    public OActivity getParent() {
        return this.parent;
    }

    public OFailureHandling getFailureHandling() {
        OFailureHandling handling = this.failureHandling;
        if (handling == null) {
            OActivity parent = this.parent;
            while (parent != null && handling == null) {
                handling = parent.failureHandling;
                parent = parent.parent;
            }
        }
        return handling;
    }

    public void setFailureHandling(OFailureHandling failureHandling) {
        this.failureHandling = failureHandling;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(super.toString());
        if (name != null) {
            buf.append('-');
            buf.append(name);
        }

        return buf.toString();
    }

    @Override
    public String digest() {
        StringBuffer buf = new StringBuffer(getClass().getSimpleName());
        buf.append('#');
        buf.append(getId());
        buf.append("{");
        List<OAgent> l = new ArrayList<OAgent>();
        l.addAll(nested);
        Collections.sort(l, new Comparator<OAgent>() {
            private String key(OAgent o) {
                return o.getClass().getSimpleName() + "#" + o.getId();
            }

            public int compare(OAgent o1, OAgent o2) {
                return key(o1).compareTo(key(o2));
            }
        });

        for (OAgent child : l) {
            buf.append(child.digest());
            buf.append(";");
        }
        buf.append("}");
        return buf.toString();
    }
}
