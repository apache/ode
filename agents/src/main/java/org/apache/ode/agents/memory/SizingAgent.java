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

package org.apache.ode.agents.memory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Stack;


/**
 * Class 
 *
 * @author $author$
 * @version $Revision$
  */
public class SizingAgent {
    private static Instrumentation instrumentation;

    /**
     * $method$
     *
     * @param options options 
     * @param instrumentation instrumentation 
     */
    public static void premain(String options, Instrumentation instrumentation) {
        SizingAgent.instrumentation = instrumentation;
        Class[] loaded = instrumentation.getAllLoadedClasses();
    }
    
    /**
     * $method$
     *
     * @param args args 
     */
    public static void main(String[] args) {
        System.out.println("Size of Object: " + sizeOf(new Object()));
        System.out.println("Size of direct subclass: " + sizeOf(new SizingAgent()));
        System.out.println("Size of Calendar: " +
            sizeOf(Calendar.getInstance()));
    }

    /**
     * Returns object size.
     */
    public static long sizeOf(Object obj) {
        if (instrumentation == null) {
        	return 0;
        }

        if (isSharedFlyweight(obj)) {
            return 0;
        }

        return instrumentation.getObjectSize(obj);
    }

    /**
     * Returns deep size of object, recursively iterating over its
     * fields and superclasses.
     */
    public static long deepSizeOf(Object obj) {
        if (instrumentation == null) {
        	return 0;
        }
        
        Map visited = new IdentityHashMap();
        Stack stack = new Stack();
        stack.push(obj);

        long result = 0;

        do {
            result += internalSizeOf(stack.pop(), stack, visited);
        } while (!stack.isEmpty());

        return result;
    }

    /**
     * Returns true if this is a well-known shared flyweight. For
     * example, interned Strings, Booleans and Number objects
     */
    private static boolean isSharedFlyweight(Object obj) {
        // optimization - all of our flyweights are Comparable
        if (obj instanceof Comparable) {
            if (obj instanceof Enum) {
                return true;
            } else if (obj instanceof String) {
                return (obj == ((String) obj).intern());
            } else if (obj instanceof Boolean) {
                return ((obj == Boolean.TRUE) || (obj == Boolean.FALSE));
            } else if (obj instanceof Integer) {
                return (obj == Integer.valueOf((Integer) obj));
            } else if (obj instanceof Short) {
                return (obj == Short.valueOf((Short) obj));
            } else if (obj instanceof Byte) {
                return (obj == Byte.valueOf((Byte) obj));
            } else if (obj instanceof Long) {
                return (obj == Long.valueOf((Long) obj));
            } else if (obj instanceof Character) {
                return (obj == Character.valueOf((Character) obj));
            }
        }

        return false;
    }

    /**
     * $method$
     *
     * @param obj obj 
     * @param visited visited 
     *
     * @return type
     */
    private static boolean skipObject(Object obj, Map visited) {
        return (obj == null) || visited.containsKey(obj) ||
        isSharedFlyweight(obj);
    }

    /**
     * $method$
     *
     * @param obj obj 
     * @param stack stack 
     * @param visited visited 
     *
     * @return type
     */
    private static long internalSizeOf(Object obj, Stack stack, Map visited) {
        if (skipObject(obj, visited)) {
            return 0;
        }

        Class clazz = obj.getClass();

        if (clazz.isArray()) {
            addArrayElementsToStack(clazz, obj, stack);
        } else {
            // add all non-primitive fields to the stack
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();

                for (Field field : fields) {
                    if (!Modifier.isStatic(field.getModifiers()) &&
                            !field.getType().isPrimitive()) {
                        field.setAccessible(true);

                        try {
                            stack.add(field.get(obj));
                        } catch (IllegalAccessException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }

                clazz = clazz.getSuperclass();
            }
        }

        visited.put(obj, null);

        return sizeOf(obj);
    }

    /**
     * $method$
     *
     * @param clazz clazz 
     * @param obj obj 
     * @param stack stack 
     */
    private static void addArrayElementsToStack(Class clazz, Object obj,
        Stack stack) {
        if (!clazz.getComponentType().isPrimitive()) {
            int length = Array.getLength(obj);

            for (int i = 0; i < length; i++) {
                stack.add(Array.get(obj, i));
            }
        }
    }
}
