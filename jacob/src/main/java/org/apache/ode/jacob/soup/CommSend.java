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

import java.lang.reflect.Method;

/**
 * Persistent store representation of a message (i.e. method application /
 * channel write) waiting for a corresponding object (i.e. channel read). This
 * structure consists of a label identifying the method that should be applied
 * to the object once it is available, and the arguments that should be applied
 * to said method.
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class CommSend extends Comm {

    private Object[] _args;

    private Method _method;

    protected CommSend() {
    }

    public CommSend(CommChannel chnl, Method method, Object[] args) {
        super(null, chnl);
        _args = args;
        _method = method;
    }

    public Method getMethod() {
        return _method;
    }

    /**
     * Get the arguments for the method application.
     */
    public Object[] getArgs() {
        return _args;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(getChannel().toString());
        buf.append(" ! ").append(_method.getName()).append('(');
        for (int i = 0; _args != null && i < _args.length; ++i) {
            if (i != 0) buf.append(',');
            buf.append(_args[i]);
        }
        buf.append(')');
        return buf.toString();
    }
}
