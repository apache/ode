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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Set;

import org.apache.ode.jacob.vpu.JacobVPU;

/**
 * Base class for constructs which rely on a Java method body to represent some
 * aspect of the process.
 */
public abstract class JacobObject implements Serializable {
    public abstract Set<Method> getImplementedMethods();

    /**
     * Get the unadorned (no package) name of this class.
     */
    protected String getClassName() {
        return getClass().getSimpleName();
    }

    protected static Object getExtension(Class extensionClass) {
        return JacobVPU.activeJacobThread().getExtension(extensionClass);
    }

    @SuppressWarnings("unchecked")
    protected static <T extends Channel> T importChannel(String channelId, Class<T> channelClass) {
        return (T) JacobVPU.activeJacobThread().importChannel(channelId, channelClass);
    }

    /**
     * Instantiation; the Java code <code>instance(new F(x,y,z))</code> is
     * equivalent to <code>F(x,y,z)</code> in the process calculus.
     *
     * @param concretion the concretion of a process template
     */
    protected static void instance(JacobRunnable concretion) {
        JacobVPU.activeJacobThread().instance(concretion);
    }

    protected <T extends Channel> T newChannel(Class<T> channelType)
            throws IllegalArgumentException
    {
        return newChannel(channelType, null);
    }

    /**
     * Channel creation; the Java code <code>Channel x = newChannel(XChannel.class) ...</code>
     * is equivalent to <code>(new x) ... </code> in the process calculus.
     */
    @SuppressWarnings("unchecked")
    protected <T extends Channel> T newChannel(Class<T> channelType, String description)
        throws IllegalArgumentException
    {
        return (T) JacobVPU.activeJacobThread().newChannel(channelType, getClassName(), description);
    }

    /**
     * Object; the Java code "object(x, ChannelListener)" is equivalent to
     * <code>x ? ChannelListener</code> in the process algebra.
     *
     * @param methodList method list for the communication reduction
     * @see JacobThread#object
     */
    protected static <T extends Channel> T object(ChannelListener<T> methodList) {
        JacobVPU.activeJacobThread().object(false, methodList);
        return methodList.getChannel();
    }

    protected static void object(boolean replication, ChannelListener methodList) {
        JacobVPU.activeJacobThread().object(replication, methodList);
    }

    protected static void object(boolean replication, ChannelListener[] methodLists) {
        JacobVPU.activeJacobThread().object(replication, methodLists);
    }

    protected static void object(boolean replication, Set<ChannelListener> methodLists) {
        JacobVPU.activeJacobThread().object(replication,
                methodLists.toArray(new ChannelListener[methodLists.size()]));
    }

    protected static <T extends Channel> T replication(ChannelListener<T> methodList) {
        JacobVPU.activeJacobThread().object(true, methodList);
        return methodList.getChannel();
    }

    /**
     * Obtain a replicated channel broadcaster.
     *
     * @param channel target channel
     * @return replicated channel broadcaster
     */
    protected static <T extends Channel> T replication(T channel) {
        // TODO: we should create a replicated wrapper here.
        return channel;
    }

    public Method getMethod(String methodName) {
        Set<Method> implementedMethods = getImplementedMethods();
        for (Method m : implementedMethods) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        throw new IllegalArgumentException("No such method \"" + methodName + "\"!");
    }

    public String toString() {
        return "<JacobObject:" + getClassName() + ">";
    }

}
