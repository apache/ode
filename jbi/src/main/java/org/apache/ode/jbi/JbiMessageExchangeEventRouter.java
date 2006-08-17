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

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.Endpoint;

class JbiMessageExchangeEventRouter implements JbiMessageExchangeProcessor {
  private static final Log __log = LogFactory.getLog(JbiMessageExchangeEventRouter.class);
  
  private OdeContext _ode;
  
  JbiMessageExchangeEventRouter(OdeContext ode) {
    _ode = ode;
  }
  
  public void onJbiMessageExchange(MessageExchange mex) throws MessagingException {
    if (mex.getRole().equals(javax.jbi.messaging.MessageExchange.Role.CONSUMER)) {
      _ode._consumer.onJbiMessageExchange(mex);
    } else if (mex.getRole().equals(javax.jbi.messaging.MessageExchange.Role.PROVIDER)) {
      OdeService svc = _ode.getService(new Endpoint(mex.getEndpoint().getServiceName(), mex.getEndpoint().getEndpointName()));
      if (svc == null)  {
        __log.error("Received message exchange for unknown service: " + mex.getEndpoint().getServiceName());
        return;
      }
      svc.onJbiMessageExchange(mex);
    } else {
      __log.debug("unexpected role: " + mex.getRole());
    }
   
  }

}
