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
import java.util.Set;

import org.apache.ode.utils.CollectionUtils;

/**
 * Base class for process abstractions. An abstraction is a parameterized
 * process template, whose instantiation is termed a <em>concretion</em>.
 * Abstractions may define a set of bound channel names or other parameters
 * which are resolved at the time of the concretion. For example the process
 * term abstraction of a memory cell:
 * <code>Cell(s,v) := s ? { read(...) = ... & write(...) = ... }</code> would
 * be represented by the following Java class: <code>
 * <pre>
 * public class Cell extends JacobRunnable {
 *     private CellChannel s;
 *
 *     private Object v;
 *
 *     public Cell(CellChannel s, Object v) {
 *         this.s = s;
 *         this.v = v;
 *     }
 *
 *     public void run() {
 *      object(new CellChannelListener(s) { read(...) {...}
 *                             write(...) {...} } );
 *    }
 * }
 * </pre>
 * </code> An example of the Java expression representing the concretion of this
 * abstraction would look like: <code>
 * <pre>
 *    .
 *    .
 *    // (new c) Cell(c,v)
 *    Integer v = Integer.valueOf(0);
 *    CellChannel c = (CellChannel)newChannel(CellChannel.class);
 *    instance(new Cell(c, v));
 *    .
 *    .
 * </pre>
 * </code>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com" />
 */
public abstract class JacobRunnable extends JacobObject {
    private static final Set<Method> IMPLEMENTED_METHODS;

    static {
        try {
            Method m = JacobRunnable.class.getMethod("run", CollectionUtils.EMPTY_CLASS_ARRAY);
            IMPLEMENTED_METHODS = Collections.singleton(m);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    public Set<Method> getImplementedMethods() {
        return IMPLEMENTED_METHODS;
    }

    /**
     * Peform the template reduction, i.e. do whatever it is that the
     * templetized process does. This method may do some combination of in-line
     * Java, and JACOB operations.
     * <p>
     * <em>Note that JACOB operations are performed in parallel, so the
     * sequencing of JACOB operations is irrelevant</em>
     */
    public abstract void run();

    public String toString() {
        return getClassName() + "(...)";
    }

}
