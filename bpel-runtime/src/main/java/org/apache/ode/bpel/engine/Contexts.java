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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.ode.bpel.dao.BpelDAOConnectionFactory;
import org.apache.ode.bpel.iapi.BindingContext;
import org.apache.ode.bpel.iapi.EndpointReferenceContext;
import org.apache.ode.bpel.iapi.MessageExchangeContext;
import org.apache.ode.bpel.iapi.Scheduler;

/**
 * Aggregation of all the contexts provided to the BPEL engine by the
 * integration layer.
 */
class Contexts {

    ConfigurationContext configurationContext;
    
    MessageExchangeContext mexContext;

    Scheduler scheduler;

    EndpointReferenceContext eprContext;

    BindingContext bindingContext;

    BpelDAOConnectionFactory dao;

    MessageExchangeInterceptor[] globalIntereceptors = new MessageExchangeInterceptor[0];

}
