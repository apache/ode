package org.apache.ode.dao.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.BpelEventFilter;
import org.apache.ode.bpel.common.InstanceFilter;
import org.apache.ode.bpel.common.ProcessFilter;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.evt.BpelEvent;

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
		return null;
	}

	public List<Date> bpelEventTimelineQuery(InstanceFilter ifilter,
			BpelEventFilter efilter) {
		// TODO Auto-generated method stub
		return null;
	}

	public void close() {
		_em = null;

	}

	public MessageExchangeDAO createMessageExchange(char dir) {
		MessageExchangeDAOImpl ret = new MessageExchangeDAOImpl(dir,this);
		_messageEx.add(ret);
		return ret;
	}

	public ProcessDAO createProcess(QName pid, QName type) {
		ProcessDAOImpl ret = new ProcessDAOImpl(pid,type,this);
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
		for (ProcessInstanceDAOImpl inst : _instances) {
			if (inst.getInstanceId() == iid ) return inst;
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
			if ( s.getScopeInstanceId() == siidl ) return s;
		}
		return null;
	}

	public void insertBpelEvent(BpelEvent event, ProcessDAO process,
			ProcessInstanceDAO instance) {
		// TODO Auto-generated method stub

	}

	public Collection<ProcessInstanceDAO> instanceQuery(InstanceFilter criteria) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ProcessInstanceDAO> instanceQuery(String expression) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<ProcessDAO> processQuery(ProcessFilter criteria) {
		// TODO Auto-generated method stub
		return null;
	}
	
	EntityManager getEntityManager() {
		return _em;
	}
	
	public void setEntityManger(EntityManager em) {
		_em = em;
	}

}
