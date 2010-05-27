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
package org.apache.ode.jacob.soup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * DOCUMENTME.
 *
 * <p>
 * Created on Feb 16, 2004 at 9:13:39 PM.
 * </p>
 *
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 *
 */

public class CommGroup extends ExecutionQueueObject {

    boolean _isReplicated;

    List<Comm> _comms = new ArrayList<Comm>();

    public CommGroup(boolean replicated) {
        _isReplicated = replicated;
    }

    /**
     * Read the value of the replication operator flag. CommRecv (channel reads)
     * with the replication flag set are left in the queue indefinately.
     *
     * @return true or false
     */
    public boolean isReplicated() {
        return _isReplicated;
    }

    public void add(Comm comm) {
        comm.setGroup(this);
        _comms.add(comm);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        for (Comm c : _comms) {
            buf.append(c).append(" + ");
        }
        buf.setLength(buf.length()-1);
        return buf.toString();
    }

    public String getDescription() {
        return toString();
    }

    public Iterator<Comm> getElements() {
        return _comms.iterator();
    }

}
