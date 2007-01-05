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

import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessFilter;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.bpel.evt.BpelEvent;

import javax.persistence.*;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="ODE_ROOT")
public class BPELDAOConnectionImpl implements BpelDAOConnection {
	
	@Transient private EntityManager _em;

	@Id @Column(name="ROOT_ID")
	private Long _id;
	
	@OneToMany(fetch=FetchType.LAZY,mappedBy="_connection",cascade={CascadeType.ALL})
	private Collection<ProcessDAOImpl> _processes = new ArrayList<ProcessDAOImpl>();

	@OneToMany(fetch=FetchType.LAZY,mappedBy="_connection",cascade={CascadeType.ALL})
	private Collection<ProcessInstanceDAOImpl> _instances = new ArrayList<ProcessInstanceDAOImpl>();

	@OneToMany(fetch=FetchType.LAZY,mappedBy="_connection",cascade={CascadeType.ALL})
	private Collection<MessageExchangeDAOImpl> _messageEx = new ArrayList<MessageExchangeDAOImpl>();

	@OneToMany(fetch=FetchType.LAZY,mappedBy="_connection",cascade={CascadeType.ALL})
	private Collection<ScopeDAOImpl> _scopes = new ArrayList<ScopeDAOImpl>();
	
	public BPELDAOConnectionImpl() {}
	public BPELDAOConnectionImpl(Long id, EntityManager em) {
		_id = id;
		_em = em;
	}
	
	public List<BpelEvent> bpelEventQuery(InstanceFilter ifilter,
			BpelEventFilter efilter) {
		// TODO Auto-generated method stub
		//return null;
		throw new UnsupportedOperationException();
		//System.out.println(ifilter.toString());
		//System.out.println(efilter.toString());
		//return null;
	}

	public List<Date> bpelEventTimelineQuery(InstanceFilter ifilter,
			BpelEventFilter efilter) {
		// TODO Auto-generated method stub
		//return null;
		throw new UnsupportedOperationException();
	}

	public void close() {
		_em = null;

	}

	public MessageExchangeDAO createMessageExchange(char dir) {
		MessageExchangeDAOImpl ret = new MessageExchangeDAOImpl(dir,this);
		_messageEx.add(ret);
		return ret;
	}

	public ProcessDAO createProcess(QName pid, QName type, String guid) {
		ProcessDAOImpl ret = new ProcessDAOImpl(pid,type,guid,this);
		
		_processes.add(ret);
		return ret;
	}

	void addInstance(ProcessInstanceDAOImpl inst) {
		_instances.add(inst);
	}
	void addScope(ScopeDAOImpl scope) {
		_scopes.add(scope);
	}
	public Long getID() {
		return _id;
	}
	public void setID(Long id) {
		_id = id;
	}
	
	public ProcessInstanceDAO getInstance(Long iid) {
		// TODO: may need a map or DB lookup here for performance
		for (ProcessInstanceDAO inst : _instances) {
			if (inst.getInstanceId().equals(iid) ) return inst;
		}
		return null;
	}

	public MessageExchangeDAO getMessageExchange(String mexid) {
		// TODO: may need a map or DB lookup here for performance
		for ( MessageExchangeDAOImpl mex : _messageEx ) {
			if ( mex.getMessageExchangeId().equals(mexid)) return mex;
		}
		return null;
	}

	public ProcessDAO getProcess(QName processId) {
		for ( ProcessDAOImpl p : _processes){
			if ( p.getProcessId().equals(processId)) return p;
		}
		return null;
	}

	public ScopeDAO getScope(Long siidl) {
		// TODO: May need a map or DB lookup here for performance
		for ( ScopeDAOImpl s : _scopes ) {
			if ( s.getScopeInstanceId().equals(siidl) ) return s;
		}
		return null;
	}

	public void insertBpelEvent(BpelEvent event, ProcessDAO process,
			ProcessInstanceDAO instance) {
		// TODO Auto-generated method stub
		//throw new UnsupportedOperationException();
		//System.out.println(event.toString());
		//System.out.println(process.toString());
		//System.out.println(instance.toString());
	}

	public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter criteria) {
        // TODO: Implement me
        return new ArrayList<ProcessInstanceDAO>(_instances);
	}

	public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
        // TODO: Implement me
        return new ArrayList<ProcessInstanceDAO>(_instances);
	}

	public Collection<ProcessDAO> processQuery(ProcessFilter criteria) {
        // TODO: Implement me
        return new ArrayList<ProcessDAO>(_processes);
	}
	
	EntityManager getEntityManager() {
		return _em;
	}
	
	public void setEntityManger(EntityManager em) {
		_em = em;
	}
	
	void removeProcess(ProcessDAOImpl p) {
		_processes.remove(p);
		
		if ( _em != null ) {
			_em.remove(p);
			_em.flush();
		}
		
	}

}
