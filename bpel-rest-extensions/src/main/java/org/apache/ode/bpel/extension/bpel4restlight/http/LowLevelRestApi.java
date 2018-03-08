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
package org.apache.ode.bpel.extension.bpel4restlight.http;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.extension.bpel4restlight.Bpel4RestLightExtensionBundle;

/**
 * This class eases HTTP method execution by providing fault handling and automated response
 * information processing
 * 
 * @author Michael Hahn (mhahn.dev@gmail.com)
 */
public class LowLevelRestApi {

    // HttpClient used for communication
    private static HttpClient httpClient = new HttpClient();

    /**
     * Executes a passed HttpMethod (method type is either PUT, POST, GET or DELETE) and returns a
     * HttpResponseMessage
     * 
     * @param method Method to execute
     * @return HttpResponseMessage which contains all information about the execution
     */
    public static HttpResponseMessage executeHttpMethod(HttpMethod method) throws FaultException {

        HttpResponseMessage responseMessage = null;

        try {
            // Execute Request
            LowLevelRestApi.httpClient.executeMethod(method);

            responseMessage = LowLevelRestApi.extractResponseInformation(method);
        } catch (HttpException e) {
            throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                    "BPEL4REST: Execution of HTTP method '" + method.getName()
                            + "' caused an exception: " + e.getMessage(),
                    e);
        } catch (IOException e) {
            throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                    "BPEL4REST: Execution of HTTP method '" + method.getName()
                            + "' caused an exception: " + e.getMessage(),
                    e);
        } finally {
            // Release connection
            method.releaseConnection();
        }

        // Extract response information and return
        return responseMessage;
    }

    /**
     * Extracts the response information from an executed HttpMethod
     * 
     * @param method The HTTP method
     * @return The extracted response information
     * @throws IOException
     */
    private static HttpResponseMessage extractResponseInformation(HttpMethod method)
            throws IOException {
        // Create and return HttpResponseMethod
        HttpResponseMessage responseMessage = new HttpResponseMessage();

        responseMessage.setStatusCode(method.getStatusCode());
        responseMessage.setResponseBody(method.getResponseBodyAsString());

        return responseMessage;
    }

}
