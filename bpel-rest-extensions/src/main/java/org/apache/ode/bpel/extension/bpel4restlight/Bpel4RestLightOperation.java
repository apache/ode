/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ode.bpel.extension.bpel4restlight;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.eapi.ExtensionContext;
import org.apache.ode.bpel.extension.bpel4restlight.http.HighLevelRestApi;
import org.apache.ode.bpel.extension.bpel4restlight.http.HttpMethod;
import org.apache.ode.bpel.extension.bpel4restlight.http.HttpResponseMessage;
import org.apache.ode.bpel.runtime.extension.AbstractSyncExtensionOperation;
import org.apache.ode.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * 
 * This class provides the implementation of the four typical REST operations GET, PUT, POST and
 * DELETE through corresponding BPEL extension activities ({@link Bpel4RestLightExtensionBundle}).
 * 
 * @author Michael Hahn (mhahn.dev@gmail.com)
 * 
 */
public class Bpel4RestLightOperation extends AbstractSyncExtensionOperation {
	
	private static final Logger logger = LoggerFactory.getLogger(Bpel4RestLightOperation.class);

    public void runSync(ExtensionContext context, Element element) throws FaultException {
        String httpMethod = element.getLocalName();
        
        // Extract requestUri
        String requestUri = Bpel4RestLightUtil.getMethodAttributeValue(context, element,
                MethodAttribute.REQUEST_URI);
        // Extract accept header
        String acceptHeader = Bpel4RestLightUtil.getMethodAttributeValue(context, element,
        		MethodAttribute.ACCEPT_HEADER);
        // Extract content type
        String contentType = Bpel4RestLightUtil.getMethodAttributeValue(context, element,
        		MethodAttribute.CONTENTTYPE);
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Starting REST call for extension activity: \n" + DOMUtils.domToString(element));
        	logger.debug("Request URI (resolved): " + requestUri);
        	logger.debug("Request accept header (resolved): " + acceptHeader);
        	logger.debug("Request content-type header (resolved): " + contentType);
        }

        HttpResponseMessage responseMessage = null;

        // Execute corresponding HttpMethod via the HighLevelRestApi
        switch (HttpMethod.valueOf(httpMethod)) {

            case PUT: {
            	try {            		
            		String requestPayload = Bpel4RestLightUtil.extractRequestPayload(context, element);
            		responseMessage = HighLevelRestApi.Put(requestUri, requestPayload, acceptHeader, contentType);
            		if (logger.isDebugEnabled()) {
            			logger.debug("Request message payload: " + requestPayload);
            		}
            	} catch(FaultException e) {
            		if (logger.isDebugEnabled()) {
            			logger.debug("Warning, request message payload will be empty");
            		}
            		responseMessage = HighLevelRestApi.Put(requestUri, acceptHeader);
            	}                                
                break;
            }

            case POST: {
            	try {            		
            		String requestPayload = Bpel4RestLightUtil.extractRequestPayload(context, element);
            		responseMessage = HighLevelRestApi.Post(requestUri, requestPayload, acceptHeader, contentType);
            		
            		if (logger.isDebugEnabled()) {
            			logger.debug("Request message payload: \n" + requestPayload);
            		}
            	} catch(FaultException e) {
            		if (logger.isDebugEnabled()) {
            			logger.debug("Warning, request message payload will be empty");
            		}
            		responseMessage = HighLevelRestApi.Post(requestUri, acceptHeader);
            	}                
                break;
            }

            case GET: {
                responseMessage = HighLevelRestApi.Get(requestUri, acceptHeader, contentType);
                break;
            }

            case DELETE: {
                responseMessage = HighLevelRestApi.Delete(requestUri, acceptHeader, contentType);
                break;
            }
            default:
                break;
        }

        processResponseMessage(responseMessage, context, element);
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Completed REST call for extension activity: \n" + DOMUtils.domToString(element));
        }
    }

    private void processResponseMessage(HttpResponseMessage responseMessage,
            ExtensionContext context, Element element) throws FaultException {
        // Write responsePayload to designated variable
        String responsePayloadVariableName = Bpel4RestLightUtil.getMethodAttributeValue(context,
                element, MethodAttribute.RESPONSE_PAYLOAD_VARIABLE);
        String statusCodeVariableName = Bpel4RestLightUtil.getMethodAttributeValue(context, element,
                MethodAttribute.STATUS_CODE_VARIABLE);
        
        
        
        String responsePayload = responseMessage.getResponseBody();
        if(responsePayload != null && !responsePayload.isEmpty()) {
        	responsePayload = responsePayload.trim();
        }
        
        int statusCode = responseMessage.getStatusCode();
        
        if (responsePayloadVariableName != null && !responsePayloadVariableName.isEmpty()) {
            Bpel4RestLightUtil.writeResponsePayload(context, responsePayload,
                    responsePayloadVariableName);
        }

        if (statusCodeVariableName != null && !statusCodeVariableName.isEmpty()) {
            Bpel4RestLightUtil.writeResponsePayload(context, statusCode,
                    statusCodeVariableName);
        }
        
        if (logger.isDebugEnabled()) {
        	logger.debug("Response status code: " + statusCode);
        	logger.debug("Response message payload: \n" + responsePayload);
        }
    }
}
