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
package org.apache.ode.bpel.memdao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.utils.ArrayUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * A very simple, in-memory implementation of the {@link CorrelatorDAO} interface.
 */
class CorrelatorDaoImpl extends DaoBaseImpl implements CorrelatorDAO {
    private static final Log __log = LogFactory.getLog(CorrelatorDaoImpl.class);

    private String _correlatorId;
    private List<MsgQueueEntry> _messages;
    private List<MessageRouteDaoImpl> _routes;
    private BpelDAOConnectionImpl _conn;

    CorrelatorDaoImpl(String correlatorId, BpelDAOConnectionImpl conn) {
        _messages = new ArrayList<MsgQueueEntry>();
        _routes = new ArrayList<MessageRouteDaoImpl>();
        _correlatorId = correlatorId;
        _conn = conn;
    }

    public MessageExchangeDAO dequeueMessage(CorrelationKey key) {
        if (__log.isDebugEnabled()) {
            __log.debug("dequeueEarliest: MATCHING correlationKey=" + key);
        }
        for (Iterator i = _messages.iterator(); i.hasNext();) {
            MsgQueueEntry mqe = (MsgQueueEntry)i.next();
            Set<CorrelationKey> keyset = (Set<CorrelationKey>)ArrayUtils.makeCollection(HashSet.class, mqe.keys);
            if ((key == null) || keyset.contains(key)) {
                i.remove();
                return mqe.message;
            }
        }
        if (__log.isDebugEnabled()) {
            __log.debug("dequeueEarliest: MATCH NOT FOUND!");
        }
        return null;
    }

    public MessageRouteDAO findRoute(CorrelationKey key) {
        if (__log.isDebugEnabled()) {
            __log.debug("findRoute: key=" + key);
        }
        for (MessageRouteDaoImpl we : _routes) {
            if ((we._ckey == null && key == null) || (we._ckey != null && key != null && we._ckey.equals(key))) {
                return we;
            }
        }
        return null;
    }

    public String getCorrelatorId() {
        return _correlatorId;
    }

    public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
        ((ProcessInstanceDaoImpl)target).removeRoutes(routeGroupId);
    }

    public void enqueueMessage(MessageExchangeDAO mex, CorrelationKey[] keys) {
        if (__log.isDebugEnabled()) {
            __log.debug("enqueueProcessInvocation: data=" + mex + " keys="
                    + ArrayUtils.makeCollection(ArrayList.class, keys));
        }

        MsgQueueEntry mqe = new MsgQueueEntry(mex, keys);
        _messages.add(mqe);
    }

    public void addRoute(String routeId,ProcessInstanceDAO target, int idx, CorrelationKey key) {
        if (__log.isDebugEnabled()) {
            __log.debug("addRoute: target=" + target + " correlationKey=" + key);
        }

        final MessageRouteDaoImpl mr = new MessageRouteDaoImpl((ProcessInstanceDaoImpl)target, routeId, key, idx);
        _conn.defer(new Runnable() {
            public void run() {
                _routes.add(mr);
            }
        });
    }


    public boolean checkRoute(CorrelationKey ckey) {
        return true;
    }

    void _removeRoutes(String routeGroupId, ProcessInstanceDaoImpl target) {
        for (Iterator<MessageRouteDaoImpl> i = _routes.iterator(); i.hasNext();) {
            MessageRouteDaoImpl we = i.next();
            if ((we._groupId.equals(routeGroupId) || routeGroupId == null) && we._instance == target) {
                i.remove();
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("{CorrelatorDaoImpl corrId=");
        buf.append(_correlatorId);
        buf.append(" waiters=");
        buf.append(_routes);
        buf.append(" messages=");
        buf.append(_messages);
        buf.append('}');

        return buf.toString();
    }

    private class MsgQueueEntry {
        public final MessageExchangeDAO message;
        public final CorrelationKey[] keys;

        private MsgQueueEntry(MessageExchangeDAO mex,
                              CorrelationKey[] keys) {
            this.message = mex;
            this.keys = keys;
        }
    }
}
