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
package org.apache.ode.bpel.evt;

import org.apache.ode.utils.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * Base interface for all bpel events.
 */
public abstract class BpelEvent implements Serializable {

    public enum TYPE {
        dataHandling, activityLifecycle, scopeHandling, instanceLifecycle, correlation;
    }

    /**
     * Bpel Event Context. In Event Listeners, use this to get Variable Data
     */
    public transient EventContext eventContext;

    private Date _timestamp = new Date();

    private int _lineNo = -1;

    public int getLineNo() {
        return _lineNo;
    }

    public void setLineNo(int lineNo) {
        _lineNo = lineNo;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

    public void setTimestamp(Date tstamp) {
        _timestamp = tstamp;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("\n" + eventName(this) + ":");

        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0) {
                try {
                    String field = method.getName().substring(3);
                    Object value = method.invoke(this, CollectionUtils.EMPTY_OBJECT_ARRAY);
                    if (value == null) {
                        continue;
                    }
                    sb.append("\n\t").append(field).append(" = ").append(value == null ? "null" : value.toString());
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return sb.toString();
    }

    public static String eventName(BpelEvent event) {
        String name = event.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    public abstract TYPE getType();

}
