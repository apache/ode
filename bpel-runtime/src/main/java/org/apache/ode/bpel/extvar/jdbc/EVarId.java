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
package org.apache.ode.bpel.extvar.jdbc;

import javax.xml.namespace.QName;

/**
 * Key for identifiying an external variable.
 *
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
class EVarId {
    final QName pid;
    final String varId;


    EVarId(QName pid, String varId) {
        this.pid = pid;
        this.varId = varId;
    }

    public boolean equals(Object o) {
        return ((EVarId)o).varId.equals(varId ) && ((EVarId)o).pid.equals(pid);
    }

    public int hashCode() {
        return varId.hashCode() ^ pid.hashCode();
    }

    public String toString() {
        return pid + "#" + varId;
    }
}
