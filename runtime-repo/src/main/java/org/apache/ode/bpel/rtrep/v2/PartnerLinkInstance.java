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

import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;

/**
 * Serializable reference to a partner link instance.
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public class PartnerLinkInstance implements Serializable {
    private static final long serialVersionUID = 1L;

    public OPartnerLink partnerLink;

    public Long scopeInstanceId;

    public PartnerLinkInstance(Long scopeInstanceId, OPartnerLink partnerLink) {
        this.partnerLink = partnerLink;
        this.scopeInstanceId = scopeInstanceId;
    }

    public boolean equals(Object obj) {
        PartnerLinkInstance other = (PartnerLinkInstance) obj;
        return partnerLink.equals(other.partnerLink) && scopeInstanceId.equals(other.scopeInstanceId);
    }

    public int hashCode() {
        return this.partnerLink.hashCode() ^ scopeInstanceId.hashCode();
    }

    public String toString() {
        return ObjectPrinter.toString(this, new Object[] { "partnerLinkDecl", partnerLink, "scopeInstanceId",
                scopeInstanceId });
    }
}
