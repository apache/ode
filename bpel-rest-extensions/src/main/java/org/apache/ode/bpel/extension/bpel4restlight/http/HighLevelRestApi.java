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

import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.extension.bpel4restlight.Bpel4RestLightExtensionBundle;

/**
 * This class wraps HTTP method functionality and thereby abstracts from low level library-specific
 * code to simplify its usage.
 * 
 * @author Michael Hahn (mhahn.dev@gmail.com)
 */
public class HighLevelRestApi {

    /**
     * This method implements the HTTP PUT Method
     * 
     * @param uri The URI of the target resource
     * @param requestPayload The payload of the request message
     * @param acceptHeaderValue The value of the accept header field to be set
     * @param contentType The contentType of the request payload
     * @return A HttpResponseMessage providing the response message payload and status code.
     * 
     * @exception FaultException
     */
    public static HttpResponseMessage Put(String uri, String requestPayload,
            String acceptHeaderValue, String contentType) throws FaultException {

        PutMethod method = new PutMethod(uri);

        HighLevelRestApi.setAcceptHeader(method, acceptHeaderValue);
        HighLevelRestApi.setContentTypeHeader(method, contentType);
        try {
            method.setRequestEntity(
                    new StringRequestEntity(requestPayload, "application/xml", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                    "BPEL4REST: Execution of HTTP method '" + method.getName()
                            + "' caused an exception.",
                    e);
        }

        HttpResponseMessage responseMessage = LowLevelRestApi.executeHttpMethod(method);
        // Remove <?xml... in front of response
        HighLevelRestApi.cleanResponseBody(responseMessage);

        return responseMessage;
    }
    
    /**
     * This method implements the HTTP PUT Method
     * 
     * @param uri The URI of the target resource
     * @param acceptHeaderValue The value of the accept header field to be set
     * @return A HttpResponseMessage providing the response message payload and status code.
     * 
     * @exception FaultException
     */
    public static HttpResponseMessage Put(String uri, 
            String acceptHeaderValue) throws FaultException {
    	PutMethod method = new PutMethod(uri);

        HighLevelRestApi.setAcceptHeader(method, acceptHeaderValue);

        HttpResponseMessage responseMessage = LowLevelRestApi.executeHttpMethod(method);
        // Remove <?xml... in front of response
        HighLevelRestApi.cleanResponseBody(responseMessage);

        return responseMessage;
    }

    /**
     * This method implements the HTTP POST Method
     * 
     * @param uri The URI of the target resource
     * @param requestPayload The payload of the request message
     * @param acceptHeaderValue The value of the accept header field to be set
     * @param contentType The contentType of the request payload
     * @return A HttpResponseMessage providing the response message payload and status code.
     * 
     * @exception FaultException
     */
    public static HttpResponseMessage Post(String uri, String requestPayload,
            String acceptHeaderValue, String contentType) throws FaultException {

        PostMethod method = null;
        if (uri.contains("?")) {
            String[] split = uri.split("\\?");
            method = new PostMethod(split[0]);
            method.setQueryString(HighLevelRestApi.createNameValuePairArrayFromQuery(split[1]));
        } else {
            method = new PostMethod(uri);
        }

        try {
            method.setRequestEntity(
                    new StringRequestEntity(requestPayload, "application/xml", "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new FaultException(Bpel4RestLightExtensionBundle.FAULT_QNAME,
                    "BPEL4REST: Execution of HTTP method '" + method.getName()
                            + "' caused an exception.",
                    e);
        }

        HighLevelRestApi.setAcceptHeader(method, acceptHeaderValue);
        HighLevelRestApi.setContentTypeHeader(method, contentType);
        HttpResponseMessage responseMessage = LowLevelRestApi.executeHttpMethod(method);
        // Remove <?xml... in front of response
        HighLevelRestApi.cleanResponseBody(responseMessage);

        return responseMessage;
    }
    
    /**
     * This method implements the HTTP POST Method
     * 
     * @param uri The URI of the target resource
     * @param acceptHeaderValue The value of the accept header field to be set
     * @return A HttpResponseMessage providing the response message payload and status code.
     * 
     * @exception FaultException
     */
    public static HttpResponseMessage Post(String uri,
            String acceptHeaderValue) throws FaultException {
    	  PostMethod method = null;
          if (uri.contains("?")) {
              String[] split = uri.split("\\?");
              method = new PostMethod(split[0]);
              method.setQueryString(HighLevelRestApi.createNameValuePairArrayFromQuery(split[1]));
          } else {
              method = new PostMethod(uri);
          }

          HighLevelRestApi.setAcceptHeader(method, acceptHeaderValue);
          HttpResponseMessage responseMessage = LowLevelRestApi.executeHttpMethod(method);
          // Remove <?xml... in front of response
          HighLevelRestApi.cleanResponseBody(responseMessage);

          return responseMessage;
    }

    /**
     * This method implements the HTTP GET Method
     * 
     * @param uri The URI of the target resource
     * @param acceptHeaderValue The value of the accept header field to be set
     * @return A HttpResponseMessage providing the response message payload and status code.
     * 
     * @exception FaultException
     */
    public static HttpResponseMessage Get(String uri, String acceptHeaderValue, String contentType)
            throws FaultException {
        GetMethod method = null;
        if (uri.contains("?")) {
            String[] split = uri.split("\\?");
            method = new GetMethod(split[0]);
            method.setQueryString(HighLevelRestApi.createNameValuePairArrayFromQuery(split[1]));
        } else {
            method = new GetMethod(uri);
        }
        HighLevelRestApi.setAcceptHeader(method, acceptHeaderValue);
        HighLevelRestApi.setContentTypeHeader(method, contentType);
        HttpResponseMessage responseMessage = LowLevelRestApi.executeHttpMethod(method);
        HighLevelRestApi.cleanResponseBody(responseMessage);
        return responseMessage;
    }

    /**
     * This method implements the HTTP DELETE Method
     * 
     * @param uri The URI of the target resource
     * @param acceptHeaderValue The value of the accept header field to be set
     * @return A HttpResponseMessage providing the response message payload and status code.
     * 
     * @exception FaultException
     */
    public static HttpResponseMessage Delete(String uri, String acceptHeaderValue, String contentType)
            throws FaultException {

        DeleteMethod method = new DeleteMethod(uri);
        HighLevelRestApi.setAcceptHeader(method, acceptHeaderValue);
        HighLevelRestApi.setContentTypeHeader(method, contentType);
        HttpResponseMessage responseMessage = LowLevelRestApi.executeHttpMethod(method);
        HighLevelRestApi.cleanResponseBody(responseMessage);
        return responseMessage;
    }

    private static NameValuePair[] createNameValuePairArrayFromQuery(String query) {
        // example:
        // csarID=Moodle.csar&serviceTemplateID={http://www.example.com/tosca/ServiceTemplates/Moodle}Moodle&nodeTemplateID={http://www.example.com/tosca/ServiceTemplates/Moodle}VmApache
        String[] pairs = query.trim().split("&");
        NameValuePair[] nameValuePairArray = new NameValuePair[pairs.length];
        int count = 0;
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            NameValuePair nameValuePair = new NameValuePair();
            nameValuePair.setName(keyValue[0]);
            nameValuePair.setValue(keyValue[1]);
            nameValuePairArray[count] = nameValuePair;
            count++;
        }
        return nameValuePairArray;
    }

    private static void setAcceptHeader(HttpMethodBase method, String value) {
        if (value != null && !value.isEmpty()) {
            method.setRequestHeader("Accept", value);
        } else {
            method.setRequestHeader("Accept", "application/xml");
        }
    }
    
    private static void setContentTypeHeader(HttpMethodBase method, String value) {
        if (value != null && !value.isEmpty()) {
            method.setRequestHeader("Content-Type", value);
        } else {
            method.setRequestHeader("Content-Type", "application/xml");
        }
    }

    private static void cleanResponseBody(HttpResponseMessage responseMessage) {
        if (responseMessage.getResponseBody() != null) {
            String temp = responseMessage.getResponseBody()
                    .replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
            responseMessage.setResponseBody(temp);
        }
    }
}
