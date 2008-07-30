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
package org.apache.ode.bpel.elang.xpath10.runtime;

import org.apache.ode.bpel.common.FaultException;

import org.jaxen.FunctionCallException;
import org.jaxen.UnresolvableException;

/**
 *  Wrap a fault in a jaxen exception
 */
public interface WrappedFaultException {
    public FaultException getFaultException();

    /**
     * Jaxenized  {@link FaultException}; Jaxen requires us to throw only exceptions
     * extending its {@link UnresolvableVariableException} so we comply.
     */
    static class JaxenUnresolvableException extends UnresolvableException implements WrappedFaultException{
        private static final long serialVersionUID = 6266231885976155458L;

        FaultException _cause;
        public JaxenUnresolvableException(FaultException e) {
            super("var");
            assert e != null;
            _cause = e;
        }

        public FaultException getFaultException() {
            return _cause;
        }
    }

    /**
     * Jaxenized  {@link FaultException}; Jaxen requires us to throw only exceptions
     * extending its {@link FunctionCallException} so we comply.
     */
    static class JaxenFunctionException extends FunctionCallException implements WrappedFaultException{
        private static final long serialVersionUID = -1915683768194623625L;
        FaultException _cause;

        public JaxenFunctionException(FaultException e) {
            super(e);
            assert e != null;
            _cause = e;
        }

        public FaultException getFaultException() {
            return _cause;
        }
    }
}
