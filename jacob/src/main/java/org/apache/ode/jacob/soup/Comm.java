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

import org.apache.ode.utils.ObjectPrinter;

/**
 * DOCUMENTME.
 * <p>
 * Created on Feb 16, 2004 at 8:44:27 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public abstract class Comm extends ExecutionQueueObject {
    private CommChannel _channel;

    private CommGroup _group;

    protected Comm() {
    }

    protected Comm(CommGroup group, CommChannel chnl) {
        _group = group;
        _channel = chnl;
    }

    public CommChannel getChannel() {
        return _channel;
    }

    public void setChannel(CommChannel channel) {
        _channel = channel;
    }

    public CommGroup getGroup() {
        return _group;
    }

    public void setGroup(CommGroup group) {
        if (_group != null) {
            throw new IllegalStateException("Attempted to call setGroup() twice!");
        }
        _group = group;
    }

    public String toString() {
        return ObjectPrinter.toString(this, new Object[] { "chnl", _channel, "group", _group });
    }
}
