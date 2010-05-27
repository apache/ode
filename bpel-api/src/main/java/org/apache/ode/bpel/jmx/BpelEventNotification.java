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
package org.apache.ode.bpel.jmx;

import org.apache.ode.bpel.evt.BpelEvent;

import javax.management.Notification;

/**
 * JMX notification used to deliver {@link org.apache.ode.bpel.evt.BpelEvent}s
 * to JMX {@link javax.management.NotificationListener}s.
 */
public class BpelEventNotification extends Notification {
    private static final long serialVersionUID = 5420803960639317141L;

    /**
     * Constructor. Creates a JMX notification with a type matching the
     * <em>class name</em> of the passed-in {@link BpelEvent} object.
     *
     * @param source
     *            originating object/{@link javax.management.ObjectName}
     * @param sequence
     *            event sequence
     * @param bpelEvent
     *            {@link BpelEvent} payload
     */
    public BpelEventNotification(Object source, long sequence,
            BpelEvent bpelEvent) {
        super(bpelEvent.getClass().getName(), source, sequence);
        setUserData(bpelEvent);
    }

    /**
     * Get the {@link BpelEvent} payload.
     *
     * @return {@link BpelEvent} payload.
     */
    public BpelEvent getBpelEvent() {
        return (BpelEvent) getUserData();
    }

}
