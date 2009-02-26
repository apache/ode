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

import org.apache.ode.jacob.JacobObject;
import org.apache.ode.utils.ObjectPrinter;

import java.lang.reflect.Method;

/**
 * DOCUMENTME.
 * <p>Created on Feb 16, 2004 at 9:23:40 PM.</p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class Continuation extends ExecutionQueueObject {
  private JacobObject _closure;
  private Method _method;
  private Object[] _args;

  public Continuation(JacobObject target, Method method, Object[] args) {
    _closure = target;
    _method = method;
    _args = args;
  }

  public JacobObject getClosure() {
    return _closure;
  }

  public Method getMethod() {
    return _method;
  }

  public Object[] getArgs() {
    return _args;
  }

  public String toString () {
    return ObjectPrinter.toString(this, new Object[] { "closure", _closure, "method", _method.getName(), "args", _args});
  }

}
