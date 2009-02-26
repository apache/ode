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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base-class for method-list objects. Method-lists objects should extends this
 * class <em>and</em> implement one <code>Channel</code> interface.
 */
public abstract class ChannelListener<CT extends Channel> extends JacobObject {
    private static Log __log = LogFactory.getLog(ChannelListener.class);

    private transient Set<Method> _implementedMethods;

    private transient CT _channel;

    protected ChannelListener(CT channel) throws IllegalStateException {
        assert getClass().getSuperclass().getSuperclass() == ChannelListener.class :
               "Inheritance in ChannelListener classes not allowed!";
        if (channel == null) {
            throw new IllegalArgumentException("Null channel!");
        }
        _channel = channel;
    }

    public CT getChannel() {
        return _channel;
    }

    public void setChannel(CT channel) {
        _channel = channel;
    }

    public Set<ChannelListener> or(ChannelListener other) {
        HashSet<ChannelListener> retval = new HashSet<ChannelListener>();
        retval.add(this);
        retval.add(other);
        return retval;
    }

    public Set<ChannelListener> or(Set<ChannelListener> other) {
        HashSet<ChannelListener> retval = new HashSet<ChannelListener>(other);
        retval.add(this);
        return retval;
    }

    public Set<Method> getImplementedMethods() {
        if (_implementedMethods == null) {
            Set<Method> implementedMethods = new HashSet<Method>();
            getImplementedMethods(implementedMethods, getClass().getSuperclass());
            _implementedMethods = Collections.unmodifiableSet(implementedMethods);
        }
        return _implementedMethods;
    }

    private static Set<Method> getImplementedMethods(Set<Method> methods, Class clazz) {
        Class[] interfaces = clazz.getInterfaces();
        for (int i=0; i<interfaces.length; ++i) {
            if (interfaces[i] != Channel.class) {
                Method[] allmethods = interfaces[i].getDeclaredMethods();
                for (int j=0; j<allmethods.length; ++j) {
                    methods.add(allmethods[j]);
                }
                getImplementedMethods(methods, interfaces[i]);
            }
        }
        return methods;
    }

    /**
     * Get a description of the object for debugging purposes.
     * 
     * @return human-readable description.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer(getClassName());
        buf.append('{');
        for (Method m : getImplementedMethods()) {
            buf.append(m.getName());
            buf.append("()");
            buf.append("&");
        }
        buf.setLength(buf.length()-1);
        buf.append('}');
        return buf.toString();
    }

    protected Log log() {
        return __log;
    }
}
