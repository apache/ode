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
import java.util.Set;


public final class ClassUtil {
    private ClassUtil() {
        // Utility class
    }

    public static Set<Method> getImplementedMethods(Set<Method> methods, Class<?> clazz) {
        // TODO: this can be optimized (some 20 times faster in my tests) by keeping a private 
        //  map of interfaces to methods: Map<Class<?>, Method[]> and just do lookups
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> iface : interfaces) {
            // TODO: the test below could be more generic...
            if (iface != ExportableChannel.class) {
                for (Method method : iface.getDeclaredMethods()) {
                    methods.add(method);
                }
                getImplementedMethods(methods, iface);
            }
        }
        return methods;
    }
}
