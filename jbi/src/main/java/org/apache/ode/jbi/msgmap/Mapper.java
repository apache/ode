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

package org.apache.ode.jbi.msgmap;

import java.util.Collection;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.wsdl.Fault;
import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.iapi.Message;

/**
 * Interface implemented by message format converters. TODO: Perhaps we should
 * move this into the engine and make it pluggable?
 */
public interface Mapper {

    /**
     * Determine if this mapper recognizes the format of the NMS message.
     *
     * @param nmsMsg
     * @return
     */
    Recognized isRecognized(NormalizedMessage nmsMsg, Operation op);

    /**
     * Convert a ODE message to NMS format. This call must only be called if
     * {@link #isRecognized(NormalizedMessage, Operation)} returned,
     * <code>true</code>.
     *
     * @param nmsMsg
     * @param odeMsg
     * @param msgdef
     * @throws MessagingException
     * @throws MessageTranslationException
     */
    void toNMS(NormalizedMessage nmsMsg, Message odeMsg, javax.wsdl.Message msgdef, QName fault) throws MessagingException,
            MessageTranslationException;

    /**
     * Convert an NMS message to ODE format. This call must only be called if
     * {@link #isRecognized(NormalizedMessage, Operation)} returned,
     * <code>true</code>.
     *
     * @param odeMsg
     * @param nmsMsg
     * @param msgdef
     * @throws MessageTranslationException
     */
    void toODE(Message odeMsg, NormalizedMessage nmsMsg, javax.wsdl.Message msgdef) throws MessageTranslationException;

    /**
     * Infer the fault type based on the message.
     * @param jbiFlt JBI fault message
     * @param faults collection of possible faults
     * @return matching fault, or null if no match
     * @throws MessageTranslationException
     */
    Fault toFaultType(javax.jbi.messaging.Fault jbiFlt, Collection<Fault> faults) throws MessageTranslationException;

    enum Recognized {
        TRUE, FALSE, UNSURE
    }


}
