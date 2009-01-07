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
package org.apache.ode.bpel.engine;

import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.jacob.soup.ReplacementMap;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A JACOB {@link ReplacementMap} implementation that eliminates unnecessary serialization
 * of the (constant) compiled process model.
 */
public class ReplacementMapImpl implements ReplacementMap {
    private OProcess _oprocess;

    public ReplacementMapImpl(OProcess oprocess) {
        _oprocess = oprocess;
    }

    public boolean isReplacement(Object obj) {
        return obj instanceof OBaseReplacementImpl;
    }

    public Object getOriginal(Object replacement) throws IllegalArgumentException {
        if (!(replacement instanceof OBaseReplacementImpl))
            throw new IllegalArgumentException("Not OBaseReplacementObject!");
        return _oprocess.getChild(((OBaseReplacementImpl)replacement)._id);
    }

    public Object getReplacement(Object original) throws IllegalArgumentException {
        if (!(original instanceof OBase))
            throw new IllegalArgumentException("Not OBase!");
        return new OBaseReplacementImpl(((OBase)original).getId());
    }

    public boolean isReplaceable(Object obj) {
        return obj instanceof OBase;
    }

    /**
     * Replacement object for serializtation of the {@link OBase} (compiled
     * BPEL) objects in the JACOB VPU.
     */
    public static final class OBaseReplacementImpl implements Externalizable {
        private static final long serialVersionUID = 1L;

        int _id;

        public OBaseReplacementImpl() {
        }
        public OBaseReplacementImpl(int id) {
            _id = id;
        }
        public void readExternal(ObjectInput in) throws IOException {
            _id = in.readInt();
        }
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeInt(_id);
        }
    }

}
