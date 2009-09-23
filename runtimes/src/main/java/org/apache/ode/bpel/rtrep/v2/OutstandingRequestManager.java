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
package org.apache.ode.bpel.rtrep.v2;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>
 * Manages receive/pick--reply matching. Keeps track of active pick/receive activities (i.e. those that have been
 * reached in the script) and their association with a message exchange (for those receive/picks that have received
 * a message). The purpose of this class is to 1) enable matching a reply activity to the corresponding receive/pick
 * activity and 2) allow us to fault out message exchanges that have not been replied to when they go out of scope.
 * </p> 
 * <p>
 * Note, this class is only used for INBOUND synchronous (request-response) operations. None of this is necessary
 * for asynchronous messages. 
 * </p> 
 */
class OutstandingRequestManager implements Serializable {
    private static final long serialVersionUID = -5556374398943757951L;

    private static final Log __log = LogFactory.getLog(OutstandingRequestManager.class);

    // BPEL associations
    private final Map<RequestIdTuple, SelectEntry> _byRequest = new HashMap<RequestIdTuple, SelectEntry>();
    private final Map<ReplyIdTuple, SelectEntry> _byReply = new HashMap<ReplyIdTuple, SelectEntry>();
    private final Map<String, SelectEntry> _byChannel = new HashMap<String, SelectEntry>();

    // RESTful associations
    private final Map<RequestResTuple, RestEntry> _byRestRid = new HashMap<RequestResTuple, RestEntry>();
    private final Map<String, RestEntry> _byRestChannel = new HashMap<String, RestEntry>();

    int findConflict(Selector selectors[]) {
        if (__log.isTraceEnabled()) {
            __log.trace(ObjectPrinter.stringifyMethodEnter("findConflict", new Object[] { "selectors", selectors}) );
        }

        Set<RequestIdTuple> workingSet = new HashSet<RequestIdTuple>(_byRequest.keySet());
        for (int i = 0; i < selectors.length; ++i) {
            final RequestIdTuple rid = new RequestIdTuple(selectors[i].plinkInstance,selectors[i].opName, selectors[i].correlationKey);
            if (workingSet.contains(rid)) {
                return i;
            }
            workingSet.add(rid);
        }
        return -1;
    }

    /**
     * Register a receive/pick with the manager. This occurs when the receive/pick is encountered in the processing of
     * the BPEL script.
     * @param pickResponseChannel response channel associated with this receive/pick
     * @param selectors selectors for this receive/pick
     */
    void register(String pickResponseChannel, Selector selectors[]) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("register", new Object[] {
                    "pickResponseChannel", pickResponseChannel, "selectors", selectors }) );

        if (_byChannel.containsKey(pickResponseChannel)) {
            String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RESPONSE CHANNEL " + pickResponseChannel;
            __log.fatal(errmsg);
            throw new IllegalArgumentException(errmsg);
        }

        SelectEntry entry = new SelectEntry(pickResponseChannel, selectors);
        for (int i = 0 ; i < selectors.length; ++i) {
            final RequestIdTuple rid = new RequestIdTuple(selectors[i].plinkInstance,selectors[i].opName, selectors[i].correlationKey);
            if (_byRequest.containsKey(rid)) {
                String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RID " + rid;
                __log.fatal(errmsg);
                throw new IllegalStateException(errmsg);
            }
            _byRequest.put(rid,  entry);
        }

        _byChannel.put(pickResponseChannel, entry);
    }

    void register(String pickResponseChannel, ResourceInstance resource, String method, String mexRef) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("register", new Object[] {
                    "pickResponseChannel", pickResponseChannel}) );

        if (_byRestChannel.containsKey(pickResponseChannel)) {
            String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RESPONSE CHANNEL " + pickResponseChannel;
            __log.fatal(errmsg);
            throw new IllegalArgumentException(errmsg);
        }

        final RequestResTuple rid = new RequestResTuple(resource, method, mexRef);
        if (_byRestRid.containsKey(rid)) {
            String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RID " + rid;
            __log.fatal(errmsg);
            throw new IllegalStateException(errmsg);
        }
        RestEntry entry = new RestEntry(pickResponseChannel);
        _byRestRid.put(rid,  entry);
        _byRestChannel.put(pickResponseChannel, entry);
    }

    /**
     * Cancel a previous registration.
     * @see #register(String, Selector[])
     * @param pickResponseChannel
     */
    void cancel(String pickResponseChannel) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("cancel", new Object[] {
                    "pickResponseChannel", pickResponseChannel }) );

        SelectEntry entry = _byChannel.remove(pickResponseChannel);
        if (entry != null) {
            while(_byRequest.values().remove(entry));
            while(_byReply.values().remove(entry));
        }

        RestEntry restEntry = _byRestChannel.remove(pickResponseChannel);
        if (restEntry != null)
            while(_byRestRid.values().remove(restEntry));
    }
    
    /**
     * Checks if an IMA has already created an association for this
     * message exchange + partnerLink + operation name
     * 
     * @param pickResponseChannel
     * @param mexRef
     * @param selectorIdx
     * @return
     */
    boolean findAssociateConflict(String pickResponseChannel, String mexRef, int selectorIdx) {
        RestEntry restEntry = _byRestChannel.get(pickResponseChannel);
        if (restEntry != null) {
            return false; // does conflicting request apply for REST?
        }
        
        SelectEntry entry = _byChannel.get(pickResponseChannel);
        if (entry != null) {
            Selector select = entry.selectors[selectorIdx];
            return _byReply.containsKey(new ReplyIdTuple(select.plinkInstance, select.opName, mexRef));
        }
        
        String errmsg = "INTERNAL ERROR: No ENTRY for RESPONSE CHANNEL " + pickResponseChannel;
        __log.fatal(errmsg);
        throw new IllegalArgumentException(errmsg);
    }

    public boolean associateEvent(PartnerLinkInstance plinkInstance, String opName, CorrelationKey key, String mexRef, String mexDAO) {
        // remove pending request first in case of fault to
        // ensure _byRequest state stays clean
        RequestIdTuple rid = new RequestIdTuple(plinkInstance, opName, key);
        SelectEntry entry = _byRequest.get(rid);
        while(_byRequest.values().remove(entry));
        
        if (entry.mexRef != null) {
            String errmsg = "INTERNAL ERROR: Duplicate ASSOCIATION for ENTRY " + entry;
            __log.fatal(errmsg);
            throw new IllegalStateException(errmsg);
        }
        entry.mexRef = mexDAO;
        
        ReplyIdTuple reply = new ReplyIdTuple(plinkInstance, opName, mexRef);
        if (_byReply.containsKey(reply)) {
            return false;
        }
        
        // create a pending reply
        _byReply.put(reply, entry);
        return true;
    }

    public void associateEvent(ResourceInstance resourceInstance, String method, String mexRef, String scopeIid) {
        RequestResTuple rid = new RequestResTuple(resourceInstance, method, mexRef);
        RestEntry entry = _byRestRid.remove(rid);
        rid.mexId = scopeIid;
        _byRestRid.put(rid, entry);
    }

    /**
     * Release the registration. This method is called when the reply activity sends a reply corresponding to the
     * registration.
     * @param plinkInstnace partner link
     * @param opName operation
     * @param mexId message exchange identifier IN THE BPEL SENSE OF THE TERM (i.e. a receive/reply disambiguator).
     * @return message exchange identifier associated with the registration that matches the parameters
     */
    public String release(PartnerLinkInstance plinkInstnace, String opName, String mexId) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("release", new Object[] {
                    "plinkInstance", plinkInstnace,
                    "opName", opName,
                    "mexId", mexId
            }) );

        // TODO use reply id
        final ReplyIdTuple rid = new ReplyIdTuple(plinkInstnace,opName, mexId);
        SelectEntry entry = _byReply.get(rid);
        if (entry == null) {
            if (__log.isDebugEnabled()) {
                __log.debug("==release: RID " + rid + " not found in " + _byReply);
            }
            return null;
        }
        while(_byChannel.values().remove(entry));
        while(_byRequest.values().remove(entry));
        while(_byReply.values().remove(entry));
        return entry.mexRef;
    }

    public String release(ResourceInstance resourceInstance, String method, String mexId) {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("release", new Object[] {
                    "resource", resourceInstance, "method", method, "mexId", mexId }) );

        final RequestResTuple rid = new RequestResTuple(resourceInstance, method, mexId);
        RestEntry entry = _byRestRid.get(rid);
        if (entry == null) {
            if (__log.isDebugEnabled()) {
                __log.debug("==release: RID " + rid + " not found in " + _byRestRid);
            }
            return null;
        }
        while(_byRestChannel.values().remove(entry));
        while(_byRestRid.values().remove(entry));
        return entry.mexRef;
    }

    /**
     * "Release" all outstanding incoming messages exchanges. Makes the object forget about
     * the previous registrations
     * @return a list of message exchange identifiers for message exchanges that were begun (receive/pick got a message)
     *            but not yet completed (reply not yet sent)
     */
    public String[] releaseAll() {
        if (__log.isTraceEnabled())
            __log.trace(ObjectPrinter.stringifyMethodEnter("releaseAll", null) );

        ArrayList<String> mexRefs = new ArrayList<String>();
        for (SelectEntry entry : _byChannel.values()) {
            if (entry.mexRef!=null)
                mexRefs.add(entry.mexRef);
        }
        _byChannel.values().clear();
        _byRequest.values().clear();
        return mexRefs.toArray(new String[mexRefs.size()]);
    }

    public String toString() {
        return ObjectPrinter.toString(this, new Object[] {
                "byRid", _byRequest,
                "byChannel", _byChannel
        });
    }

    /**
     * Tuple identifying an outstanding request (i.e. a receive,pick, or onMessage on a
     * synchronous operation needing a reply).
     */
    private class RequestIdTuple  implements Serializable {
        private static final long serialVersionUID = -1059389611839777482L;
        /** On which partner link it was received. */
        PartnerLinkInstance partnerLink;
        /** Name of the operation. */
        String opName;
        /** Correlation key identifier. */
        String key;

        /** Constructor. */
        private RequestIdTuple(PartnerLinkInstance partnerLink, String opName, CorrelationKey correlationKey) {
            this.partnerLink = partnerLink;
            this.opName = opName;
            this.key = correlationKey == null ? "" : correlationKey.toCanonicalString();
        }

        public int hashCode() {
            return this.partnerLink.hashCode() ^ this.opName.hashCode() ^ key.hashCode();
        }

        public boolean equals(Object obj) {
            RequestIdTuple other = (RequestIdTuple) obj;
            return other.partnerLink.equals(partnerLink) &&
                    other.opName.equals(opName) &&
                    other.key.equals(key);
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] {
                    "partnerLink", partnerLink,
                    "opName", opName,
                    "correltionKey", key
            });
        }
    }
    
    /**
     * Tuple identifying an outstanding reply (i.e. a reply).
     */
    private class ReplyIdTuple  implements Serializable {
        private static final long serialVersionUID = -2993419819851933718L;
        /** On which partner link it was received. */
        PartnerLinkInstance partnerLink;
        /** Name of the operation. */
        String opName;
        /** Message exchange identifier. */
        String mexId;

        /** Constructor. */
        private ReplyIdTuple(PartnerLinkInstance partnerLink, String opName, String mexId) {
            this.partnerLink = partnerLink;
            this.opName = opName;
            this.mexId = mexId == null ? "" : mexId;
        }

        public int hashCode() {
            return this.partnerLink.hashCode() ^ this.opName.hashCode() ^ this.mexId.hashCode();
        }

        public boolean equals(Object obj) {
            ReplyIdTuple other = (ReplyIdTuple) obj;
            return other.partnerLink.equals(partnerLink) &&
                    other.opName.equals(opName) &&
                    other.mexId.equals(mexId);
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] {
                    "partnerLink", partnerLink,
                    "opName", opName,
                    "mexId", mexId
            });
        }
    }

    private class RequestResTuple  implements Serializable {
        private static final long serialVersionUID = -1059359612839777482L;
        /** Name of the operation. */
        ResourceInstance resource;
        /** Message exchange identifier. */
        String method;
        /** Message exchange identifier. */
        String mexId;

        /** Constructor. */
        private RequestResTuple(ResourceInstance resource, String method, String mexId) {
            this.resource = resource;
            this.method = method;
            this.mexId = mexId;
        }

        public int hashCode() {
            return this.resource.hashCode() ^ this.method.hashCode() ^ this.mexId.hashCode();
        }

        public boolean equals(Object obj) {
            RequestResTuple other = (RequestResTuple) obj;
            return other.resource.equals(resource) && other.method.equals(method) && other.mexId.equals(mexId);
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] {"url", resource, "method", method, "mexId", mexId});
        }
    }

    private class SelectEntry implements Serializable {
        private static final long serialVersionUID = -583743124656582887L;
        final String pickResponseChannel;
        final Selector[] selectors;
        String mexRef;

        private SelectEntry(String pickResponseChannel, Selector[] selectors) {
            this.pickResponseChannel = pickResponseChannel;
            this.selectors = selectors;
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] {
                    "pickResponseChannel", pickResponseChannel,
                    "selectors", selectors,
                    "mexRef", mexRef
            });
        }
    }

    private class RestEntry implements Serializable {
        private static final long serialVersionUID = -583733124656582887L;
        final String pickResponseChannel;
        String mexRef;

        private RestEntry(String pickResponseChannel) {
            this.pickResponseChannel = pickResponseChannel;
        }

        public String toString() {
            return ObjectPrinter.toString(this, new Object[] {
                    "pickResponseChannel", pickResponseChannel,
                    "mexRef", mexRef
            });
        }
    }
}
