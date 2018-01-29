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

/**
 * This class is used to wrap information of a HTTP response message in a library neutral format
 * 
 * @author Michael Hahn (mhahn.dev@gmail.com)
 */
public class HttpResponseMessage {

    private int statusCode;
    private String responseBody;

    /**
     * @return the statusCode
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * @param statusCode the statusCode to set
     */
    protected void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * @return the responseBody
     */
    public String getResponseBody() {
        return this.responseBody;
    }

    /**
     * @param responseBody the responseBody to set
     */
    protected void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

}
