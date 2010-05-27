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

import org.apache.ode.bpel.evt.BpelEvent;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @author Matthieu Riou <mriou at apache dot org>
 */
@Entity
@Table(name="ODE_EVENT")
@NamedQueries({
    @NamedQuery(name=EventDAOImpl.SELECT_EVENT_IDS_BY_PROCESS, query="select e._id from EventDAOImpl as e where e._instance._process = :process"),
    @NamedQuery(name=EventDAOImpl.DELETE_EVENTS_BY_IDS, query="delete from EventDAOImpl as e where e._id in (:ids)"),
    @NamedQuery(name=EventDAOImpl.DELETE_EVENTS_BY_INSTANCE, query="delete from EventDAOImpl as e where e._instance = :instance")
})
public class EventDAOImpl extends OpenJPADAO {
    public final static String SELECT_EVENT_IDS_BY_PROCESS = "SELECT_EVENT_IDS_BY_PROCESS";
    public final static String DELETE_EVENTS_BY_IDS = "DELETE_EVENTS_BY_IDS";
    public final static String DELETE_EVENTS_BY_INSTANCE = "DELETE_EVENTS_BY_INSTANCE";

    @Id @Column(name="EVENT_ID")
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long _id;
    @Basic @Column(name="TSTAMP")
    private Timestamp _tstamp;
    @Basic @Column(name="TYPE")
    private String _type;
    @Basic @Column(name="DETAIL")
    private String _detail;

    /** Scope identifier, possibly null. */
    @Basic @Column(name="SCOPE_ID")
    private Long _scopeId;

    @ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST}) @Column(name="PROCESS_ID")
    private ProcessDAOImpl _process;
    @ManyToOne(fetch= FetchType.LAZY,cascade={CascadeType.PERSIST})	@Column(name="INSTANCE_ID")
    private ProcessInstanceDAOImpl _instance;
    @Lob  @Column(name="DATA")
    private BpelEvent _event;

    public BpelEvent getEvent() {
        return _event;
    }

    public void setEvent(BpelEvent event) {
        _event = event;
    }

    public String getDetail() {
        return _detail;
    }

    public void setDetail(String detail) {
        _detail = detail;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long id) {
        _id = id;
    }

    public ProcessInstanceDAOImpl getInstance() {
        return _instance;
    }

    public void setInstance(ProcessInstanceDAOImpl instance) {
        _instance = instance;
    }

    public ProcessDAOImpl getProcess() {
        return _process;
    }

    public void setProcess(ProcessDAOImpl process) {
        _process = process;
    }

    public Timestamp getTstamp() {
        return _tstamp;
    }

    public void setTstamp(Timestamp tstamp) {
        _tstamp = tstamp;
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public Long getScopeId() {
        return _scopeId;
    }

    public void setScopeId(Long scopeId) {
        _scopeId = scopeId;
    }
}
