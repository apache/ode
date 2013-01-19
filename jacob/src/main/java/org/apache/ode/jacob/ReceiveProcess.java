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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@SuppressWarnings("serial")
public abstract class ReceiveProcess<T extends Channel> extends ChannelListener {
    private transient Set<Method> _implementedMethods;
    private transient Channel channel;
    private T receiver;
    
    protected ReceiveProcess(T channel, T receiver) throws IllegalStateException {
        assert getClass().getSuperclass().getSuperclass() == ChannelListener.class :
            "Inheritance in ChannelListener classes not allowed!";
        if (channel == null) {
            throw new IllegalArgumentException("Null channel!");
        }
        this.channel = channel;
        this.receiver = receiver;
    }

    public T receiver() {
        return receiver;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Set<Method> getImplementedMethods() {
        if (_implementedMethods == null) {
            Set<Method> implementedMethods = new HashSet<Method>();
            ClassUtil.getImplementedMethods(implementedMethods, receiver.getClass());
            _implementedMethods = Collections.unmodifiableSet(implementedMethods);
        }
        return _implementedMethods;
    }
}
