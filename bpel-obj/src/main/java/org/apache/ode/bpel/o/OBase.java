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

import java.io.Serializable;


/**
 * Base class for compiled BPEL objects.
 */
public class OBase implements Serializable {

    static final long serialVersionUID = -1L  ;

    /** Our identifier, in terms of our parent. */
    private final int       _id;
    private final OProcess  _owner;

    public DebugInfo debugInfo;

    protected OBase(OProcess owner) {
        _owner = owner;
        if (owner == null) {
            _id = 0;
        } else {
            _id = ++_owner._childIdCounter;
            _owner._children.add(this);
        }
        assert _id == 0 || _owner != null;
    }

    public OProcess getOwner() {
        return (OProcess) (_owner == null ? this : _owner);
    }

    public int hashCode() {
        return _id;
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof OBase))
            return false;

        OBase other = (OBase) obj;
        return (_id == 0 && other._id == 0) || _id == other._id && other._owner.equals(_owner);
    }

    public int getId() {
        return _id;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(getClass().getSimpleName());
        buf.append('#');
        buf.append(_id);
        return buf.toString();
    }

    public void dehydrate() {
        if (debugInfo != null) {
            debugInfo.description = null;
            debugInfo.extensibilityElements = null;
            debugInfo = null;
        }
    }

    public String digest() {
        return "";
    }
}
