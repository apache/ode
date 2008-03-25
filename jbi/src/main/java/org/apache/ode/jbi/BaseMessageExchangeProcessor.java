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
package org.apache.ode.jbi;

import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Base message exchange processor; handles common JBI message exchange conditions and delegates
 * actual invocation to subclass.
 */
public abstract class BaseMessageExchangeProcessor implements JbiMessageExchangeProcessor {
    final Log LOG = LogFactory.getLog(getClass());

    DeliveryChannel _channel;
    
    public BaseMessageExchangeProcessor(DeliveryChannel channel) {
        _channel = channel;
    }

    /**
     * Handle the JBI message exchange
     */
    public void onJbiMessageExchange(MessageExchange jbiMex) throws MessagingException {
        if (jbiMex.getStatus() == ExchangeStatus.DONE) {
            // everything went right, nothing to do
            return;
        }
        if (jbiMex.getStatus() == ExchangeStatus.ERROR) {
            LOG.error("Error reported for JBI mex "+jbiMex, jbiMex.getError());
            return;
        }

        if (jbiMex.getPattern().equals(MessageExchangePattern.IN_ONLY)) {
            boolean success = false;
            Exception err = null;
            try {
                invoke((InOnly) jbiMex);
                success = true;
            } catch (Exception ex) {
                LOG.error("Error invoking "+getClass(), ex);
                err = ex;
            } finally {
                if (!success) {
                    jbiMex.setStatus(ExchangeStatus.ERROR);
                    if (err != null && jbiMex.getError() == null)
                        jbiMex.setError(err);
                } else {
                    if (jbiMex.getStatus() == ExchangeStatus.ACTIVE)
                        jbiMex.setStatus(ExchangeStatus.DONE);
                }
                _channel.send(jbiMex);
            }
        } else if (jbiMex.getPattern().equals(MessageExchangePattern.IN_OUT)) {
            InOut mex = (InOut) jbiMex;
            boolean success = false;
            Exception err = null;
            try {
                NormalizedMessage response = invoke(mex);
                mex.setOutMessage(response);
                _channel.send(jbiMex);
                success = true;
            } catch (Exception ex) {
                LOG.error("Error invoking "+getClass(), ex);
                err = ex;
            } catch (Throwable t) {
                LOG.error("Unexpected error invoking ODE.", t);
                err = new RuntimeException(t);
            } finally {
                // If we got an error that wasn't sent.  
                if (jbiMex.getStatus() == ExchangeStatus.ACTIVE && !success) {
                    if (err != null && jbiMex.getError() == null)  {
                        jbiMex.setError(err);
                    }
                    jbiMex.setStatus(ExchangeStatus.ERROR);     
                    _channel.send(jbiMex);
                }       
            }
        } else {
            LOG.error("JBI MessageExchange " + jbiMex.getExchangeId() + " is of an unsupported pattern "
                    + jbiMex.getPattern());
            jbiMex.setStatus(ExchangeStatus.ERROR);
            jbiMex.setError(new Exception("Unknown message exchange pattern: " + jbiMex.getPattern()));
        }

    }

    /**
     * Invoke with an In-Out message exchange pattern. 
     */
    public abstract NormalizedMessage invoke(InOut mex) throws MessagingException;

    /**
     * Invoke with an In-Only message exchange pattern.
     */
    public abstract void invoke(InOnly mex) throws MessagingException;
    
}
