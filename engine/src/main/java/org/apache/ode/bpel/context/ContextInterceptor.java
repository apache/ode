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
package org.apache.ode.bpel.context;

import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.rapi.ContextData;
import org.apache.ode.bpel.rapi.IOContext;
import org.w3c.dom.Element;

/**
 * Context Interceptors can map message headers (or even payload) to
 * context information that is attached to process models.
 * 
 * @author Tammo van Lessen
 */
public interface ContextInterceptor {
	
	/**
	 * Configures the interceptor. This method will be called immediatedly after
	 * instantiation of the implementing class. The passed element will contain the
	 * configuration elements given in the deploy.xml. In case of a declaration in
	 * xxx-ode.properties, the method won't be called.
	 * 
	 * @param configuration
	 */
    void configure(Element configuration);
    
    /**
     * Translates the data stored within the context object into SOAP headers or
     * vice versa.
     * 
     * If direction is OUTBOUND, context data must be converted into message headers
     * if direction is INBOUND, context data must be extracted from the message.
     */
    void process(ContextData ctx, MessageExchangeDAO mexdao, IOContext.Direction dir) throws ContextException;

}
