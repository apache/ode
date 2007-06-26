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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.iapi.InvocationStyle;
import org.apache.ode.bpel.iapi.Message;
import org.apache.ode.bpel.iapi.MessageExchange;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange;
import org.apache.ode.bpel.iapi.Scheduler;
import org.apache.ode.bpel.iapi.MyRoleMessageExchange.CorrelationStatus;
import org.apache.ode.bpel.intercept.AbortMessageExchangeException;
import org.apache.ode.bpel.intercept.FaultMessageExchangeException;
import org.apache.ode.bpel.intercept.InterceptorInvoker;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor;
import org.apache.ode.bpel.intercept.MessageExchangeInterceptor.InterceptorContext;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides an implementation of the {@link MyRoleMessageExchange} inteface for interactions performed in the
 * {@link InvocationStyle#RELIABLE} style.
 * 
 * @author Maciej Szefler
 */
class ReliableMyRoleMessageExchangeImpl extends MyRoleMessageExchangeImpl implements MyRoleMessageExchange {

    private static final Log __log = LogFactory.getLog(ReliableMyRoleMessageExchangeImpl.class);

    public static final int TIMEOUT = 2 * 60 * 1000;

    
    public ReliableMyRoleMessageExchangeImpl(BpelEngineImpl engine, String mexId) {
        super(engine, mexId);

        // RELIABLE means we are bound to a transaction
        _txflag = true;
    }


    public void invokeReliable() {
        // For reliable, we MUST HAVE A TRANSACTION!
        assertTransaction();

        // Cover the case where invoke was already called. 
        if (_status == Status.REQUEST)
            return;
        
        if (_status != Status.NEW)
            throw new BpelEngineException("Invalid state: " + _status);
        
        final BpelProcess target = _engine.route(_callee, _request);
        if (target == null) {
            if (__log.isWarnEnabled())
                __log.warn(__msgs.msgUnknownEPR("" + _epr));

            _cstatus = MyRoleMessageExchange.CorrelationStatus.UKNOWN_ENDPOINT;
            setFailure(MessageExchange.FailureType.UNKNOWN_ENDPOINT, null, null);
            save();
            return;
        }

        if (!processInterceptors(InterceptorInvoker.__onBpelServerInvoked, getDAO())) {
            throw new BpelEngineException("Intercepted.");
        }
        
        if (__log.isDebugEnabled())
            __log.debug("invoke() EPR= " + _epr + " ==> " + target);
        setStatus(Status.REQUEST);
        save(getDAO());
        scheduleInvoke(target);
    }


}
