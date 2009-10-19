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

package org.apache.ode.dao.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name = "ODE_CORRELATOR")
@NamedQueries( { @NamedQuery(name = CorrelatorDAOImpl.DELETE_CORRELATORS_BY_PROCESS, query = "delete from CorrelatorDAOImpl as c where c._process = :process") })
public class CorrelatorDAOImpl extends OpenJPADAO implements CorrelatorDAO {
    private static Log __log = LogFactory.getLog(CorrelatorDAOImpl.class);
    public final static String DELETE_CORRELATORS_BY_PROCESS = "DELETE_CORRELATORS_BY_PROCESS";
    private final static String ROUTE_BY_CKEY_HEADER = "select route from MessageRouteDAOImpl as route where route._correlator._process._processType = :ptype and route._correlator._correlatorKey = :corrkey";

    @Id
    @Column(name = "CORRELATOR_ID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @SuppressWarnings("unused")
    private Long _correlatorId;
    @Basic
    @Column(name = "CORRELATOR_KEY")
    private String _correlatorKey;
    @OneToMany(targetEntity = MessageRouteDAOImpl.class, mappedBy = "_correlator", fetch = FetchType.EAGER, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private Collection<MessageRouteDAOImpl> _routes = new ArrayList<MessageRouteDAOImpl>();
    @OneToMany(targetEntity = MessageExchangeDAOImpl.class, mappedBy = "_correlator", fetch = FetchType.LAZY, cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private Collection<MessageExchangeDAOImpl> _exchanges = new ArrayList<MessageExchangeDAOImpl>();
    @ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST })
    @Column(name = "PROC_ID")
    private ProcessDAOImpl _process;

    public CorrelatorDAOImpl() {
    }

    public CorrelatorDAOImpl(String correlatorKey, ProcessDAOImpl process) {
        _correlatorKey = correlatorKey;
        _process = process;
    }

    public void addRoute(String routeGroupId, ProcessInstanceDAO target, int index, CorrelationKeySet correlationKeySet, String routePolicy) {
        if (__log.isDebugEnabled()) {
            __log.debug("addRoute " + routeGroupId + " " + target + " " + index + " " + correlationKeySet + " " + routePolicy);
        }
        MessageRouteDAOImpl mr = new MessageRouteDAOImpl(correlationKeySet, routeGroupId, index, (ProcessInstanceDAOImpl) target, this, routePolicy);
        _routes.add(mr);
        getEM().flush();
    }

    public MessageExchangeDAO dequeueMessage(CorrelationKeySet correlationKeySet) {
        // TODO: this thing does not seem to be scalable: loading up based on a correlator???
        for (Iterator<MessageExchangeDAOImpl> itr = _exchanges.iterator(); itr.hasNext();) {
            MessageExchangeDAOImpl mex = itr.next();
            if (mex.getCorrelationKeySet().isRoutableTo(correlationKeySet, false)) {
                itr.remove();
                return mex;
            }
        }
        return null;
    }

    public void enqueueMessage(MessageExchangeDAO mex, CorrelationKeySet correlationKeySet) {
        MessageExchangeDAOImpl mexImpl = (MessageExchangeDAOImpl) mex;
        mexImpl.setCorrelationKeySet(correlationKeySet);
        _exchanges.add(mexImpl);
        mexImpl.setCorrelator(this);
    }

    public Collection<CorrelatorMessageDAO> getAllMessages() {
        return new ArrayList<CorrelatorMessageDAO>(_exchanges);
    }

    @SuppressWarnings("unchecked")
    public List<MessageRouteDAO> findRoute(CorrelationKeySet correlationKeySet) {
        if (__log.isDebugEnabled()) {
            __log.debug("findRoute " + correlationKeySet);
        }
        List<CorrelationKeySet> subSets = correlationKeySet.findSubSets();
        Query qry = getEM().createQuery(generateSelectorQuery(ROUTE_BY_CKEY_HEADER, subSets));
        qry.setParameter("ptype", _process.getType().toString());
        qry.setParameter("corrkey", _correlatorKey);
        for (int i = 0; i < subSets.size(); i++) {
            qry.setParameter("s" + i, subSets.get(i).toCanonicalString());
        }

        List<MessageRouteDAO> candidateRoutes = (List<MessageRouteDAO>) qry.getResultList();
        if (candidateRoutes.size() > 0) {
            List<MessageRouteDAO> matchingRoutes = new ArrayList<MessageRouteDAO>();
            boolean routed = false;
            for (int i = 0; i < candidateRoutes.size(); i++) {
                MessageRouteDAO route = candidateRoutes.get(i);
                if ("all".equals(route.getRoute())) {
                    matchingRoutes.add(route);
                } else {
                    if (!routed) {
                        matchingRoutes.add(route);
                    }
                    routed = true;
                }
            }
            if (__log.isDebugEnabled()) {
                __log.debug("findRoute found " + matchingRoutes);
            }
            return matchingRoutes;
        } else {
            if (__log.isDebugEnabled()) {
                __log.debug("findRoute found nothing");
            }
            return null;
        }
    }

    private String generateSelectorQuery(String header, List<CorrelationKeySet> subSets) {
        StringBuffer filterQuery = new StringBuffer(header);

        if (subSets.size() == 1) {
            filterQuery.append(" and route._correlationKey = :s0");
        } else if (subSets.size() > 1) {
            filterQuery.append(" and route._correlationKey in(");
            for (int i = 0; i < subSets.size(); i++) {
                if (i > 0) {
                    filterQuery.append(", ");
                }
                filterQuery.append(":s").append(i);
            }
            filterQuery.append(")");
        }

        return filterQuery.toString();
    }

    public String getCorrelatorId() {
        return _correlatorKey;
    }

    public void setCorrelatorId(String newId) {
        _correlatorKey = newId;
    }

    public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
        // remove route across all correlators of the process
        ((ProcessInstanceDAOImpl) target).removeRoutes(routeGroupId);
    }

    void removeLocalRoutes(String routeGroupId, ProcessInstanceDAO target) {
        if (__log.isDebugEnabled()) {
            __log.debug("removeLocalRoutes " + routeGroupId);
        }
        boolean flush = false;
        for (Iterator<MessageRouteDAOImpl> itr = _routes.iterator(); itr.hasNext();) {
            MessageRouteDAOImpl mr = itr.next();
            if (mr.getGroupId().equals(routeGroupId) && mr.getTargetInstance().equals(target)) {
                if (__log.isDebugEnabled()) {
                    __log.debug("removing " + mr.getCorrelationKey() + " " + mr.getIndex() + " " + mr.getRoute());
                }
                itr.remove();
                getEM().remove(mr);
                flush = true;
            }
        }
        if (flush) {
            getEM().flush();
        }
    }

    public Collection<MessageRouteDAO> getAllRoutes() {
        return new ArrayList<MessageRouteDAO>(_routes);
    }
}
