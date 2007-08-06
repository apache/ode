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
package org.apache.ode.jacob;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * Standard debug information for channels, objects (channel reads), and
 * messages (channel writes).
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 */
public class DebugInfo implements Serializable {
	private static final long serialVersionUID = -4877379887875005010L;

	/** Stringified representation of the instance. */
    private String _creator = "unknown";

    /** Stack trace */
    private StackTraceElement[] _stackTrace = new StackTraceElement[0];

    public void setCreator(String creator) {
        _creator = creator;
    }

    public String getCreator() {
        return _creator;
    }

    public void setLocation(StackTraceElement[] location) {
        _stackTrace = location;
    }

    public StackTraceElement[] getLocation() {
        return _stackTrace;
    }

    public void printStackTrace(PrintStream pw) {
        pw.println(_creator);

        for (int i=0; i<_stackTrace.length; i++) {
            pw.println("\tat " + _stackTrace[i]);
        }
    }
}
