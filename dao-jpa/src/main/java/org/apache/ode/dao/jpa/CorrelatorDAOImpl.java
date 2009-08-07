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
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.MessageRouteDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Entity
@Table(name="ODE_CORRELATOR")
@NamedQueries({
    @NamedQuery(name="RouteByCKey", query="SELECT route " +
            "FROM MessageRouteDAOImpl as route " +
            "WHERE route._correlationKey = :ckey " +
                   "and route._correlator._process._processType = :ptype " +
                   "and route._correlator._correlatorKey = :corrkey"),
    @NamedQuery(name=CorrelatorDAOImpl.DELETE_CORRELATORS_BY_PROCESS, query="delete from CorrelatorDAOImpl as c where c._process = :process")
})
public class CorrelatorDAOImpl extends OpenJPADAO implements CorrelatorDAO {
    public final static String DELETE_CORRELATORS_BY_PROCESS = "DELETE_CORRELATORS_BY_PROCESS";

    @Id @Column(name="CORRELATOR_ID")
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long _correlatorId;
    @Basic @Column(name="CORRELATOR_KEY")
    private String _correlatorKey;
    @OneToMany(targetEntity=MessageRouteDAOImpl.class,mappedBy="_correlator",fetch=FetchType.EAGER,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Collection<MessageRouteDAOImpl> _routes = new ArrayList<MessageRouteDAOImpl>();
    @OneToMany(targetEntity=MessageExchangeDAOImpl.class,mappedBy="_correlator",fetch=FetchType.LAZY,cascade={CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Collection<MessageExchangeDAOImpl> _exchanges = new ArrayList<MessageExchangeDAOImpl>();
    @ManyToOne(fetch= FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROC_ID")
    private ProcessDAOImpl _process;

    public CorrelatorDAOImpl(){}
    public CorrelatorDAOImpl(String correlatorKey, ProcessDAOImpl process) {
        _correlatorKey = correlatorKey;
        _process = process;
    }

    public void addRoute(String routeGroupId, ProcessInstanceDAO target, int index, CorrelationKey correlationKey) {
        MessageRouteDAOImpl mr = new MessageRouteDAOImpl(correlationKey,
                routeGroupId, index, (ProcessInstanceDAOImpl) target, this);
        _routes.add(mr);
    }

    public MessageExchangeDAO dequeueMessage(CorrelationKey correlationKey) {
        MessageExchangeDAOImpl toRemove = null;
        for (Iterator<MessageExchangeDAOImpl> itr=_exchanges.iterator(); itr.hasNext();){
            MessageExchangeDAOImpl mex = itr.next();
            if (mex.getCorrelationKeys().contains(correlationKey)) {
                toRemove = mex;
            }
        }
        _exchanges.remove(toRemove);
        return toRemove;
    }

    public void enqueueMessage(MessageExchangeDAO mex,
                               CorrelationKey[] correlationKeys) {
        MessageExchangeDAOImpl mexImpl = (MessageExchangeDAOImpl) mex;
        for (CorrelationKey key : correlationKeys ) {
            mexImpl.addCorrelationKey(key);
        }
        _exchanges.add(mexImpl);
        mexImpl.setCorrelator(this);

    }

    public MessageRouteDAO findRoute(CorrelationKey correlationKey) {
        Query qry = getEM().createNamedQuery("RouteByCKey");
        qry.setParameter("ckey", correlationKey.toCanonicalString());
        qry.setParameter("ptype", _process.getType().toString());
        qry.setParameter("corrkey", _correlatorKey);
        List<MessageRouteDAO> routes = (List<MessageRouteDAO>) qry.getResultList();
        if (routes.size() > 0) return routes.get(0);
        else return null;
    }

    public String getCorrelatorId() {
        return _correlatorKey;
    }

    public void removeRoutes(String routeGroupId, ProcessInstanceDAO target) {
        // remove route across all correlators of the process
        ((ProcessInstanceDAOImpl)target).removeRoutes(routeGroupId);
    }

    void removeLocalRoutes(String routeGroupId, ProcessInstanceDAO target) {
        boolean flush = false;
        for (Iterator<MessageRouteDAOImpl> itr=_routes.iterator(); itr.hasNext(); ) {
            MessageRouteDAOImpl mr = itr.next();
            if ( mr.getGroupId().equals(routeGroupId) && mr.getTargetInstance().equals(target)) {
                itr.remove();
                getEM().remove(mr);
                flush = true;
            }
        }
        if (flush) {
            getEM().flush();
        }
    }
}
