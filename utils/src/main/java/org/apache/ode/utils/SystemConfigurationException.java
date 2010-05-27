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
package org.apache.ode.utils;

/**
 * An exception to encapsulate issues with system configuration. Examples
 * include the inability to find required services (e.g., XML parsing).
 */
public class SystemConfigurationException extends RuntimeException {

    private static final long serialVersionUID = -2330515949287155695L;

    /**
     * Construct a new instance with the specified message.
     *
     * @param message
     *            a descriptive message.
     * @see RuntimeException#RuntimeException(java.lang.String)
     */
    public SystemConfigurationException(String message) {
        super(message);
    }

    /**
     * Construct a new instance with the specified message and a
     * {@link Throwable} that triggered this exception.
     *
     * @param message
     *            a descriptive message
     * @param cause
     *            the cause
     * @see RuntimeException#RuntimeException(java.lang.String,
     *      java.lang.Throwable)
     */
    public SystemConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a new instance with the specified {@link Throwable} as the root
     * cause.
     *
     * @param cause
     *            the cause
     * @see RuntimeException#RuntimeException(java.lang.Throwable)
     */
    public SystemConfigurationException(Throwable cause) {
        super(cause);
    }
}
