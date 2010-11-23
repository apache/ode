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
package org.apache.ode.bpel.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.wsdl.OperationType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.runtime.PartnerLinkInstance;
import org.apache.ode.bpel.runtime.Selector;
import org.apache.ode.utils.ObjectPrinter;

/**
 * <p>
 * This class handles behaviour of IMAs (Inbound Message Activities) as specified in WS BPEL.
 * This includes detecting conflictingReceive and conflictingRequest faults.
 * </p>
 */
public class IMAManager2 implements Serializable {
    private static final long serialVersionUID = -5556374398943757951L;

    private static final Log __log = LogFactory.getLog(IMAManager2.class);

    // holds rid for registered IMAs
    public final Map<RequestIdTuple, Entry> _byRid = new HashMap<RequestIdTuple, Entry>();
    // holds outstanding rid that are now waiting to reply (Open IMAs)
    public final Map<OutstandingRequestIdTuple, String> _byOrid = new HashMap<OutstandingRequestIdTuple, String>();
    public final Map<String, Entry> _byChannel = new HashMap<String, Entry>();

    /**
     * finds conflictingReceive
     *
     * @param selectors
     * @return
     */
    int findConflict(Selector selectors[]) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("findConflict", new Object[] { "selectors", selectors }));
        }

        Set<RequestIdTuple> workingSet = new HashSet<RequestIdTuple>(_byRid.keySet());
        for (int i = 0; i < selectors.length; ++i) {
            final RequestIdTuple rid = new RequestIdTuple(selectors[i].plinkInstance, selectors[i].opName, selectors[i].correlationKeySet);
            if (workingSet.contains(rid)) {
                return i;
            }
            workingSet.add(rid);
        }
        return -1;
    }

    /**
     * Register IMA
     *
     * @param pickResponseChannel
     *            response channel associated with this receive/pick
     * @param selectors
     *            selectors for this receive/pick
     */
    void register(String pickResponseChannel, Selector selectors[]) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("register", new Object[] { "pickResponseChannel", pickResponseChannel, "selectors", selectors }));
        }

        if (_byChannel.containsKey(pickResponseChannel)) {
            String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RESPONSE CHANNEL " + pickResponseChannel;
            __log.fatal(errmsg);
            throw new IllegalArgumentException(errmsg);
        }

        Entry entry = new Entry(pickResponseChannel, selectors);
        for (int i = 0; i < selectors.length; ++i) {
            final RequestIdTuple rid = new RequestIdTuple(selectors[i].plinkInstance, selectors[i].opName, selectors[i].correlationKeySet);
            if (_byRid.containsKey(rid)) {
                String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RID " + rid;
                __log.fatal(errmsg);
                throw new IllegalStateException(errmsg);
            }
            _byRid.put(rid, entry);
        }

        _byChannel.put(pickResponseChannel, entry);
    }

    /**
     * Registers Open IMA.
     * It doesn't open IMA for non two way operations.
     *
     * @param partnerLink
     * @param opName
     * @param mexId
     * @param mexRef
     * @return
     */
    String processOutstandingRequest(PartnerLinkInstance partnerLink, String opName, String mexId, String mexRef) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("process", new Object[] { "partnerLinkInstance", partnerLink, "operationName", opName, "messageExchangeId", mexId, "mexRef", mexRef }));
        }
        final OutstandingRequestIdTuple orid = new OutstandingRequestIdTuple(partnerLink, opName, mexId);
        if (_byOrid.containsKey(orid)) {
            //conflictingRequest found
            return mexRef;
        }
        // We convert into outstanding request only for in-out operations (pending release operation)
        if (partnerLink.partnerLink.getMyRoleOperation(opName).getStyle().equals(OperationType.REQUEST_RESPONSE)) {
            _byOrid.put(orid, mexRef);
        }
        return null;
    }

    /**
     * This is used to remove IMA from registered state.
     *
     * @see #register(String, Selector[])
     * @param pickResponseChannel
     */
    void cancel(String pickResponseChannel, boolean isTimer) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("cancel", new Object[] { "pickResponseChannel", pickResponseChannel }));

        Entry entry = _byChannel.remove(pickResponseChannel);
        if (entry != null) {
            while (_byRid.values().remove(entry));
        } else if (!isTimer){
            String errmsg = "INTERNAL ERROR: No ENTRY for RESPONSE CHANNEL " + pickResponseChannel;
            __log.fatal(errmsg);
            throw new IllegalArgumentException(errmsg);
        }
    }

    /**
     * Release Open IMA.
     *
     * @param plinkInstnace
     *            partner link
     * @param opName
     *            operation
     * @param mexId
     *            message exchange identifier IN THE BPEL SENSE OF THE TERM (i.e. a receive/reply disambiguator).
     * @return message exchange identifier associated with the registration that matches the parameters
     */
    public String release(PartnerLinkInstance plinkInstnace, String opName, String mexId) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("release", new Object[] { "plinkInstance", plinkInstnace, "opName", opName, "mexId", mexId }));

        final OutstandingRequestIdTuple orid = new OutstandingRequestIdTuple(plinkInstnace, opName, mexId);
        String mexRef = _byOrid.remove(orid);
        if (mexRef == null) {
            if (__log.isDebugEnabled()) {
                __log.debug("==release: ORID " + orid + " not found in " + _byOrid);
            }
            return null;
        }
        return mexRef;
    }

    /**
     * "Release" all Open IMAs
     *
     * @return a list of message exchange identifiers for message exchanges that were begun (receive/pick got a message) but not yet completed (reply not yet sent)
     */
    public String[] releaseAll() {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("releaseAll", null));

        ArrayList<String> mexRefs = new ArrayList<String>();
        while (!_byOrid.isEmpty()) {
            String mexRef = _byOrid.entrySet().iterator().next().getValue();
            mexRefs.add(mexRef);
            _byOrid.values().remove(mexRef);
        }
        return mexRefs.toArray(new String[mexRefs.size()]);
    }

    public String toString() {
        return ObjectPrinter.toString(this, new Object[] { "byRid", _byRid, "byOrid", _byOrid, "byChannel", _byChannel });
    }

    public static class RequestIdTuple implements Serializable {
        private static final long serialVersionUID = -1059389611839777482L;
        /** On which partner link it was received. */
        PartnerLinkInstance partnerLink;
        /** Name of the operation. */
        String opName;
        /** cset */
        CorrelationKeySet ckeySet;

        /** Constructor. */
        RequestIdTuple(PartnerLinkInstance partnerLink, String opName, CorrelationKeySet ckeySet) {
            this.partnerLink = partnerLink;
            this.opName = opName;
            this.ckeySet = ckeySet;
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] { "partnerLink", partnerLink, "opName", opName, "cSet", ckeySet});
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((ckeySet == null) ? 0 : ckeySet.hashCode());
            result = prime * result
                    + ((opName == null) ? 0 : opName.hashCode());
            result = prime * result
                    + ((partnerLink == null) ? 0 : partnerLink.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof RequestIdTuple)) {
                return false;
            }
            RequestIdTuple other = (RequestIdTuple) obj;
            if (ckeySet == null) {
                if (other.ckeySet != null) {
                    return false;
                }
            } else if (!ckeySet.equals(other.ckeySet)) {
                return false;
            }
            if (opName == null) {
                if (other.opName != null) {
                    return false;
                }
            } else if (!opName.equals(other.opName)) {
                return false;
            }
            if (partnerLink == null) {
                if (other.partnerLink != null) {
                    return false;
                }
            } else if (!partnerLink.equals(other.partnerLink)) {
                return false;
            }
            return true;
        }
    }

    public static class OutstandingRequestIdTuple implements Serializable {
        private static final long serialVersionUID = -1059389611839777482L;
        /** On which partner link it was received. */
        PartnerLinkInstance partnerLink;
        /** Name of the operation. */
        String opName;
        /** Message exchange identifier. */
        String mexId;

        /** Constructor. */
        OutstandingRequestIdTuple(PartnerLinkInstance partnerLink, String opName, String mexId) {
            this.partnerLink = partnerLink;
            this.opName = opName;
            this.mexId = mexId == null ? "" : mexId;
        }

        public int hashCode() {
            return this.partnerLink.hashCode() ^ this.opName.hashCode() ^ this.mexId.hashCode();
        }

        public boolean equals(Object obj) {
            OutstandingRequestIdTuple other = (OutstandingRequestIdTuple) obj;
            return other.partnerLink.equals(partnerLink) && other.opName.equals(opName) && other.mexId.equals(mexId);
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] { "partnerLink", partnerLink, "opName", opName, "mexId", mexId });
        }
    }

    public static class Entry implements Serializable {
        private static final long serialVersionUID = -583743124656582887L;
        final String pickResponseChannel;
        public Selector[] selectors;

        Entry(String pickResponseChannel, Selector[] selectors) {
            this.pickResponseChannel = pickResponseChannel;
            this.selectors = selectors;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime
                    * result
                    + ((pickResponseChannel == null) ? 0 : pickResponseChannel
                            .hashCode());
            result = prime * result + Arrays.hashCode(selectors);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Entry other = (Entry) obj;
            if (pickResponseChannel == null) {
                if (other.pickResponseChannel != null)
                    return false;
            } else if (!pickResponseChannel.equals(other.pickResponseChannel))
                return false;
            if (!Arrays.equals(selectors, other.selectors))
                return false;
            return true;
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] { "pickResponseChannel", pickResponseChannel, "selectors", selectors });
        }
    }
}
