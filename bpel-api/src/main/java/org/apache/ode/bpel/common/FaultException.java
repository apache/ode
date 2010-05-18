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
package org.apache.ode.bpel.common;

import javax.xml.namespace.QName;


/**
 * <p>Encapsulates an exception that should result in a known named fault being thrown
 * within a BPEL process.</p>
 * <p>As per BPEL specification, appendix A.</p>
 */
public class FaultException extends Exception {
    private static final long serialVersionUID = 389190682205802035L;
    private QName _qname;

    /**
     * Create a new instance.
     *
     * @param qname   the <code>QName</code> of the fault
     * @param message a descriptive message for the exception
     */
    public FaultException(QName qname, String message) {
        super(message);
        _qname = qname;
    }

    public FaultException(QName qname) {
        super(qname.toString());
        _qname = qname;
    }

    public FaultException(QName qname, String message, Throwable cause) {
        super(message, cause);
        this._qname = qname;
    }

    public FaultException(QName qname, Throwable cause) {
        super(cause);
        this._qname = qname;
    }

    /**
     * Get the (official) <code>QName</code> of this fault.
     *
     * @return the <code>QName</code> of the fault
     */
    public QName getQName() {
        return _qname;
  }
}
