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

import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.iapi.BpelEngine;
import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext;

/**
 * Implementation of the {@link org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext}
 * interface.
 * @author Maciej Szefler (m s z e f l e r @ g m a i l . c o m)
 *
 */
public class InterceptorContextImpl implements InterceptorContext{
    private ProcessDAO _processDao;
    private BpelDAOConnection _connection;
    private ProcessConf _pconf;
    private BpelEngine _bpelEngine;
    private BpelProcess _bpelProcess;

    public InterceptorContextImpl(BpelDAOConnection connection, ProcessDAO processDAO, ProcessConf pconf, BpelEngine bpelEngine, BpelProcess bpelProcess) {
        _connection = connection;
        _processDao = processDAO;
        _pconf = pconf;
        _bpelEngine = bpelEngine;
        _bpelProcess = bpelProcess;
    }

    public BpelDAOConnection getConnection() {
        return _connection;
    }

    public ProcessDAO getProcessDAO() {
        return _processDao;
    }

    public ProcessConf getProcessConf() {
        return _pconf;
    }

    public BpelEngine getBpelEngine() {
        return _bpelEngine;
    }

    public BpelProcess getBpelProcess() {
        return _bpelProcess;
    }

}
