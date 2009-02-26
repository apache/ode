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
package org.apache.ode.bpel.dao;

import org.apache.ode.bpel.common.CorrelationKeySet;

import org.apache.ode.bpel.common.CorrelationKey;

/**
 * Data access object representing a message consumer. A message consumer
 * represents an unsatisfied BPEL <code>pick</code> or <code>receive</code>
 * activity.
 */
public interface MessageRouteDAO  {

    /**
     * Get the BPEL process instance to which this consumer belongs.
     *
     * @return the process instance to which this consumer belongs
     */
    ProcessInstanceDAO getTargetInstance();

    String getGroupId();

    int getIndex();

    String getRoute();

    /**
     * Returns a correlation key set for the message route
     * @return
     */
    public CorrelationKeySet getCorrelationKeySet();

    void setCorrelationKeySet(CorrelationKeySet keySet);

    void setCorrelationKey(CorrelationKey key);

    CorrelationKey getCorrelationKey();

}
