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
package org.apache.ode.jacob.soup;

import org.apache.ode.jacob.ChannelListener;

/**
 * Persistent store representation of an object (i.e. channel read) waiting for
 * a message (i.e. channel write / method application). This class maintains an
 * opaque byte array which specifies the continuation (the exact nature of this
 * data is determined by the main JACOB VPU), as well as information regarding
 * which methods are supported by the object, and whether the read is of a
 * replicated variety.
 */
public class CommRecv extends Comm {
    private ChannelListener _continuation;

    protected CommRecv() {
    }

    public CommRecv(CommChannel chnl, ChannelListener continuation) {
        super(null, chnl);
        _continuation = continuation;
    }

    /**
     * Get the continuation for this object (channel read). The continuation is
     * what happens after a message is matched to the object. It is up to the
     * JACOB VPU to determine what is placed here, but it will generally consist
     * of some serialized representation of an appropriate ChannelListener
     * object (see {@link ChannelListener}.
     * 
     * @return byte array representing the serialized form of the continuation
     */
    public ChannelListener getContinuation() {
        return _continuation;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(getChannel().toString());
        buf.append(" ? ").append(_continuation.toString());
        return buf.toString();
    }
}
