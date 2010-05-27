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

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.CorrelationKeySet;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="ODE_MESSAGE_ROUTE")
@NamedQueries ({
    @NamedQuery(name=MessageRouteDAOImpl.DELETE_MESSAGE_ROUTES_BY_INSTANCE_IDS, query="delete from MessageRouteDAOImpl as r where r._instanceId in(:instanceIds)"),
    @NamedQuery(name=MessageRouteDAOImpl.DELETE_MESSAGE_ROUTES_BY_INSTANCE, query="delete from MessageRouteDAOImpl as r where r._processInst = :instance")
})
public class MessageRouteDAOImpl implements MessageRouteDAO {
    public final static String DELETE_MESSAGE_ROUTES_BY_INSTANCE = "DELETE_MESSAGE_ROUTES_BY_INSTANCE";
    public final static String DELETE_MESSAGE_ROUTES_BY_INSTANCE_IDS = "DELETE_MESSAGE_ROUTES_BY_INSTANCE_IDS";

    @Id @Column(name="MESSAGE_ROUTE_ID")
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long _id;
    @Basic @Column(name="GROUP_ID")
    private String _groupId;
    @Basic @Column(name="ROUTE_INDEX")
    private int _index;
    @Basic @Column(name="CORRELATION_KEY")
    private String _correlationKey;
    @Basic @Column(name="ROUTE_POLICY", length=16)
    private String _routePolicy;

    @SuppressWarnings("unused")
    @Basic @Column(name="PROCESS_INSTANCE_ID", insertable=false, updatable=false, nullable=true)
    private int _instanceId;
    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_INSTANCE_ID")
    private ProcessInstanceDAOImpl _processInst;

    @ManyToOne(fetch= FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="CORR_ID")
    @SuppressWarnings("unused")
    private CorrelatorDAOImpl _correlator;

    public MessageRouteDAOImpl() {}
    public MessageRouteDAOImpl(CorrelationKeySet keySet, String groupId, int index,
                               ProcessInstanceDAOImpl processInst, CorrelatorDAOImpl correlator, String routePolicy) {
        _correlationKey = keySet.toCanonicalString();
        _groupId = groupId;
        _index = index;
        _processInst = processInst;
        _correlator = correlator;
        _routePolicy = routePolicy;
    }

    public Long getId() {
        return _id;
    }

    public CorrelationKey getCorrelationKey() {
        return new CorrelationKey(_correlationKey);
    }

    public void setCorrelationKey(CorrelationKey key) {
        _correlationKey = key.toCanonicalString();
    }

    public String getGroupId() {
        return _groupId;
    }

    public int getIndex() {
        return _index;
    }

    public ProcessInstanceDAO getTargetInstance() {
        return _processInst;
    }

    public String getRoute() {
        return _routePolicy;
    }

    public CorrelationKeySet getCorrelationKeySet() {
        return new CorrelationKeySet(_correlationKey);
    }

    public void setCorrelationKeySet(CorrelationKeySet keySet) {
        _correlationKey = keySet.toCanonicalString();
    }
}
