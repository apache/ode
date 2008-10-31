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

package org.apache.ode.bpel.iapi;

import javax.xml.namespace.QName;

import java.util.concurrent.Future;

/**
 * Extension of the {@link org.apache.ode.bpel.iapi.MessageExchange} interface
 * that is provided by the engine for message-exchanges where the engine acts as
 * the server (i.e. where the engine is "invoked").
 */
public interface MyRoleMessageExchange extends MessageExchange {

    /**
     * Enumeration of message correlation results.
     */
    public enum CorrelationStatus {
        /** The EPR is not associated with a process. */
        UKNOWN_ENDPOINT,

        /** The request resulted in the creation of a new instance. */
        CREATE_INSTANCE,

        /** The request was matched to an existing instance.fail */
        MATCHED,

        /** The request did not match an existing instance and was queued. */
        QUEUED
    }

    /**
     * Get the correlation state of the the invocation. An invocation will
     * either create a new process instance, match an existing instance, or be
     * queued for consumption by an instance in the future (see
     * {@link CorrelationType} for details).
     * 
     * @return correlation state of the invocation
     */
    CorrelationStatus getCorrelationStatus();

    /**
     * "Invoke" a process hosted by the BPEL engine. The state of the invocation
     * may be obtained by a call to the {@link MessageExchange#getStatus()}
     * method. It is possible that the response for the operation is not
     * immediately available (i.e the call to {@link #invoke(Message)} will
     * return before a response is available). In such cases,
     * {@link MessageExchange#getStatus()} == {@link Status#ASYNC} and the
     * integration layer will receive an asynchronous notification from the BPEL
     * engine via the
     * {@link MessageExchangeContext#onAsyncReply(MyRoleMessageExchange)} when
     * the response become available.
     */
    @SuppressWarnings("unchecked")
    Future invoke(Message request);

    /**
     * Complete the message, exchange: indicates that the client has receive the
     * response (if any).
     */
    void complete();

    /**
     * Associate a client key with this message exchange.
     * 
     * @param clientKey
     */
    void setClientId(String clientKey);

    /**
     * Get the previously associated client key for this exchange.
     * 
     * @return
     */
    String getClientId();

    /**
     * Get the name of the service targetted in this message exchange.
     * 
     * @return service name
     */
    QName getServiceName();
    
    /**
     * Should be called by the external partner when it's done with the
     * message exchange. Ncessary for a better resource management and
     * proper mex cleanup.
     */
    void release(boolean instanceSucceeded);
}
