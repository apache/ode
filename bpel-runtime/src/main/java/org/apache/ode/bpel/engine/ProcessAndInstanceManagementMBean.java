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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;

import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessStore;


/**
 * Standard MBean exposing ODE's process model and instance management API
 *
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class ProcessAndInstanceManagementMBean implements DynamicMBean {

    private MBeanInfo _mbeanInfo;
    private ProcessAndInstanceManagementImpl _pm;

    private static final List<String> __excludes = new ArrayList<String>();
        static {
        __excludes.add("hashCode");
        __excludes.add("equals");
        __excludes.add("getClass");
        __excludes.add("wait");
        __excludes.add("notify");
        __excludes.add("notifyAll");
        __excludes.add("toString");
    }

    private final static Hashtable<String, Class<?>> primitives = new Hashtable<String, Class<?>>();
    static {
        primitives.put(Boolean.TYPE.toString(), Boolean.TYPE);
        primitives.put(Character.TYPE.toString(), Character.TYPE);
        primitives.put(Byte.TYPE.toString(), Byte.TYPE);
        primitives.put(Short.TYPE.toString(), Short.TYPE);
        primitives.put(Integer.TYPE.toString(), Integer.TYPE);
        primitives.put(Long.TYPE.toString(), Long.TYPE);
        primitives.put(Float.TYPE.toString(), Float.TYPE);
        primitives.put(Double.TYPE.toString(), Double.TYPE);
    }

    public ProcessAndInstanceManagementMBean(BpelServer server, ProcessStore store) {
        this(new ProcessAndInstanceManagementImpl(server, store));
    }

    /**
     */
    public ProcessAndInstanceManagementMBean(ProcessAndInstanceManagementImpl pm) {
        _pm = pm;
        List<MBeanOperationInfo> exposedOperations = new ArrayList<MBeanOperationInfo>();
        for (Method m : pm.getClass().getMethods()) {
            if (!__excludes.contains(m.getName())) {
                exposedOperations.add(new MBeanOperationInfo(m.getName(), m));
            }
        }

        MBeanAttributeInfo[] attributes = new MBeanAttributeInfo[] {};
        MBeanConstructorInfo[] constructors = new MBeanConstructorInfo[] {};
        MBeanOperationInfo[] operations = new MBeanOperationInfo[exposedOperations.size()];
        operations = (MBeanOperationInfo[]) exposedOperations.toArray(operations);
        MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[] {};

        _mbeanInfo = new MBeanInfo(getClass().getName(), "Process and Instance Management",
                attributes, constructors, operations, notifications);
    }

    public Object getAttribute(String attribute)
            throws AttributeNotFoundException, MBeanException,
            ReflectionException {
        throw new UnsupportedOperationException();
    }

    public AttributeList getAttributes(String[] attributes) {
        throw new UnsupportedOperationException();
    }

    public MBeanInfo getMBeanInfo() {
        return _mbeanInfo;
    }

    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            Method m = _pm.getClass().getMethod(actionName, findTypes(_pm.getClass().getClassLoader(), signature));
            if (m == null) {
                throw new ReflectionException(new NoSuchMethodException(actionName));
            }
            return m.invoke(_pm, params);
        } catch (Exception e) {
            throw new ReflectionException(e);
		} finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
    }

    private Class[] findTypes(ClassLoader loader, String[] signature) throws ReflectionException {
        if (signature == null)
            return null;
        final Class[] result = new Class[signature.length];
        try {
            for (int i = 0; i < signature.length; i++) {
                result[i] = primitives.get(signature[i]);
                if (result[i] == null) {
                    result[i] = Class.forName(signature[i], false, loader);
                }
            }
        } catch (ClassNotFoundException e) {
            throw new ReflectionException(e);
        }
        return result;
    }

    public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException, InvalidAttributeValueException,
            MBeanException, ReflectionException {
        throw new UnsupportedOperationException();
    }

    public AttributeList setAttributes(AttributeList attributes) {
        throw new UnsupportedOperationException();
    }

}
