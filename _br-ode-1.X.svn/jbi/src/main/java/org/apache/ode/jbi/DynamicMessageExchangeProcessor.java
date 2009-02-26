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
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMElement;
import org.apache.ode.il.DynamicService;
import org.apache.ode.il.OMUtils;

/**
 * JBI message exchange processor that uses {@link DynamicService} for reflection-based method
 * dispatch.
 */
public class DynamicMessageExchangeProcessor<T> extends BaseMessageExchangeProcessor {

    T _service;
    
    public DynamicMessageExchangeProcessor(T service, DeliveryChannel channel) {
        super(channel);
        _service = service;
    }

    @Override
    public void invoke(InOnly mex) throws MessagingException {
        try {
            DynamicService<T> service = new DynamicService<T>(_service);
            OMElement payload = OMUtils.toOM(mex.getInMessage().getContent());
            service.invoke(mex.getOperation().getLocalPart(), payload);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NormalizedMessage invoke(InOut mex) throws MessagingException {
        try {
            DynamicService<T> service = new DynamicService<T>(_service);
            OMElement payload = OMUtils.toOM(mex.getInMessage().getContent());
            OMElement response = service.invoke(mex.getOperation().getLocalPart(), payload);
            NormalizedMessage nresponse = null;
            if (response != null) {
                nresponse = mex.createMessage();
                nresponse.setContent(new DOMSource(OMUtils.toDOM(response)));
            }
            return nresponse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
