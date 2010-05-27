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
package org.apache.ode.bpel.runtime;

/**
 * Exception used by the runtime to indicate a problem with the execution context. This
 * is for dealing with conditions where the runtime expects certain things of the context
 * and the context does not oblige. These are what one might call "internal errors", for
 * example if a message is received and it does not have the required parts, this execption
 * is thrown, since the runtime expects received messages to be of the correct form.
 *
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class InvalidContextException extends RuntimeException {

    public InvalidContextException() {
        super();
    }

    public InvalidContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidContextException(String message) {
        super(message);
    }

    public InvalidContextException(Throwable cause) {
        super(cause);
    }

}
