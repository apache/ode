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
package org.apache.ode.jacob.vpu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.ode.jacob.Channel;
import org.apache.ode.jacob.soup.CommChannel;
import org.apache.ode.utils.CollectionUtils;

public class ChannelFactory {
    private static final Method METHOD_OBJECT_EQUALS;

    private static final Method METHOD_CHANNEL_EXPORT;

    static {
        try {
            METHOD_OBJECT_EQUALS = Object.class.getMethod("equals", new Class[] { Object.class });
        } catch (Exception e) {
            throw new AssertionError("No equals(Object) method on Object!");
        }

        try {
            METHOD_CHANNEL_EXPORT = Channel.class.getMethod("export", CollectionUtils.EMPTY_CLASS_ARRAY);
        } catch (Exception e) {
            throw new AssertionError("No export() method on Object!");
        }
    }

    public static Object getBackend(Channel channel) {
        ChannelInvocationHandler cih = (ChannelInvocationHandler) Proxy.getInvocationHandler(channel);
        return cih._backend;
    }

    public static Channel createChannel(CommChannel backend, Class type) {
        InvocationHandler h = new ChannelInvocationHandler(backend);
        Class[] ifaces = new Class[] { Channel.class, type };
        Object proxy = Proxy.newProxyInstance(Channel.class.getClassLoader(), ifaces, h);
        return (Channel) proxy;
    }

    public static final class ChannelInvocationHandler implements InvocationHandler {
        private CommChannel _backend;

        ChannelInvocationHandler(CommChannel backend) {
            _backend = backend;
        }

        public String toString() {
            return _backend.toString();
        }

        public boolean equals(Object other) {
            return ((ChannelInvocationHandler) other)._backend.equals(_backend);
        }

        public int hashCode() {
            return _backend.hashCode();
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                if (method.equals(METHOD_OBJECT_EQUALS)) {
                    return Boolean.valueOf(this.equals(Proxy.getInvocationHandler(args[0])));
                }
                return method.invoke(this, args);
            }
            if (method.equals(METHOD_CHANNEL_EXPORT)) {
                return JacobVPU.activeJacobThread().exportChannel((Channel) proxy);
            }
            return JacobVPU.activeJacobThread().message((Channel) proxy, method, args);
        }
    } // class ChannelInvocationHandler

}
