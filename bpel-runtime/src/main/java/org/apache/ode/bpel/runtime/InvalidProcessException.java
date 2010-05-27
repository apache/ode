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
 * A run-time exception indicating that the process is invalid. This should not
 * normally occur: that is, a process should be found to be invalid before it is
 * executed. Therefore, this can be viewed as a rather rare and serious
 * condition.
 */
public class InvalidProcessException extends RuntimeException {
    private static final long serialVersionUID = 9184731070635430159L;

    public final static int DEFAULT_CAUSE_CODE = 0;

    public final static int RETIRED_CAUSE_CODE = 1;

    public final static int TOO_MANY_INSTANCES_CAUSE_CODE = 2;

    public final static int TOO_MANY_PROCESSES_CAUSE_CODE = 3;

    public final static int TOO_HUGE_PROCESSES_CAUSE_CODE = 4;

    private final int causeCode;

    public InvalidProcessException(String msg, Throwable cause) {
        super(msg, cause);
        this.causeCode = DEFAULT_CAUSE_CODE;
    }

    public InvalidProcessException(String msg) {
        super(msg);
        this.causeCode = DEFAULT_CAUSE_CODE;
    }

    public InvalidProcessException(Exception cause) {
        super(cause);
        this.causeCode = DEFAULT_CAUSE_CODE;
    }

    /**
     * @param causeCode
     */
    public InvalidProcessException(final int causeCode) {
        super();
        this.causeCode = causeCode;
    }

    /**
     * @param message
     * @param causeCode
     */
    public InvalidProcessException(String message, final int causeCode) {
        super(message);
        this.causeCode = causeCode;
    }

    /**
     * @return the cause code
     */
    public int getCauseCode() {
        return causeCode;
    }
}
