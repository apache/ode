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
package org.apache.ode.bpel.intercept;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.Message;

/**
 * Exception thrown by {@link org.apache.ode.bpel.intercept.MessageExchangeInterceptor}
 * implementations that is used to indicate that the processing of the exchange should
 * be aborted with a fault.
 * @author Maciej Szefler
 */
public final class FaultMessageExchangeException extends AbortMessageExchangeException {
    private static final long serialVersionUID = 1L;

    private QName _faultName;
    private Message _faultData;

    public FaultMessageExchangeException(String errmsg, QName faultName, Message faultData) {
        super(errmsg);

        _faultName = faultName;
        _faultData = faultData;
    }

    public QName getFaultName() {
        return _faultName;
    }

    public Message getFaultData() {
        return _faultData;
    }
}
