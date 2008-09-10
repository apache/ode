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
package org.apache.ode.bpel.rtrep.v1.xpath20;

import org.apache.ode.bpel.common.FaultException;

/**
 *  Wrap a fault in a jaxen exception
 */
public class WrappedFaultException extends RuntimeException {
    private static final long serialVersionUID = -2677245631724501573L;

    public FaultException _fault;

    public WrappedFaultException(String message) {
        super(message);
    }

    public WrappedFaultException(FaultException message) {
        super(message);
        _fault = message;
    }

    public WrappedFaultException(String message, FaultException cause) {
        super(message, cause);
        _fault = cause;
    }

}

