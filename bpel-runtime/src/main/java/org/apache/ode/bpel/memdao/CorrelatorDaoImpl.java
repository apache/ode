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
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;

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

    public MessageExchangeDAO dequeueMessage(CorrelationKeySet instanceKeySet) {
        if (__log.isDebugEnabled()) {
            __log.debug("dequeueEarliest: MATCHING correlationKey=" + instanceKeySet);
        }
        for (Iterator<MsgQueueEntry> i = _messages.iterator(); i.hasNext();) {
            MsgQueueEntry mqe = i.next();
            CorrelationKeySet aKeySet = mqe.keySet;
            if (aKeySet.isRoutableTo(instanceKeySet, false)) {
                i.remove();
                return mqe.message;
            }
        }
        if (__log.isDebugEnabled()) {
            __log.debug("dequeueEarliest: MATCH NOT FOUND!");
        }
        return null;
    }

    public List<MessageRouteDAO> findRoute(CorrelationKeySet keySet) {
        List<MessageRouteDAO> routes = new ArrayList<MessageRouteDAO>();

        assert keySet != null;

        if (__log.isDebugEnabled()) {
            __log.debug("findRoute: keySet=" + keySet);
        }
        boolean routed = false;
        for (MessageRouteDaoImpl route : _routes) {
            assert route._ckeySet != null;

            if(keySet.isRoutableTo(route._ckeySet, "all".equals(route.getRoute()))) {
                if ("all".equals(route.getRoute()))  {
                    routes.add(route);
                } else {
                    if (!routed) {
                        routes.add(route);
                    }
                    routed = true;
                }
            }
        }

        return routes;
    }

    public String getCorrelatorId() {
        return _correlatorId;
    }

    public void setCorrelatorId(String newId) {
        _correlatorId = newId;
    }

    public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
        ((ProcessInstanceDaoImpl)target).removeRoutes(routeGroupId);
    }

    public Collection<MessageRouteDAO> getAllRoutes() {
        return new ArrayList<MessageRouteDAO>(_routes);
    }

    public Collection<CorrelatorMessageDAO> getAllMessages() {
        return new ArrayList<CorrelatorMessageDAO>(_messages);
    }

    public void enqueueMessage(MessageExchangeDAO mex, CorrelationKeySet keySet) {
        if (__log.isDebugEnabled()) {
            __log.debug("enqueueProcessInvocation: data=" + mex + " keys=" + keySet);
        }

        MsgQueueEntry mqe = new MsgQueueEntry(mex, keySet);
        _messages.add(mqe);
    }

    public void addRoute(String routeId,ProcessInstanceDAO target, int idx, CorrelationKeySet keySet, String routePolicy) {
        if (__log.isDebugEnabled()) {
            __log.debug("addRoute: target=" + target + " correlationKeySet=" + keySet);
        }

        final MessageRouteDaoImpl mr = new MessageRouteDaoImpl((ProcessInstanceDaoImpl)target, routeId, keySet, idx, routePolicy);
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

    private class MsgQueueEntry implements CorrelatorMessageDAO {
        public MessageExchangeDAO message;
        public CorrelationKeySet keySet;

        private MsgQueueEntry(MessageExchangeDAO mex,
                              CorrelationKeySet keySet) {
            this.message = mex;
            this.keySet = keySet;
        }

        public CorrelationKey getCorrelationKey() {
            return keySet.iterator().next();
        }

        public void setCorrelationKey(CorrelationKey ckey) {
            keySet = new CorrelationKeySet();
            keySet.add(ckey);
        }
    }

    public boolean checkRoute(CorrelationKeySet correlationKeySet) {
        return true;
    }

}
