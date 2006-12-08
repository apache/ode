package org.apache.ode.dao.jpa;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.CorrelatorDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.dao.ScopeDAO;
import org.apache.ode.bpel.dao.CorrelationSetDAO;

@Entity
@Table(name="ODE_PROCESS")
public class ProcessDAOImpl implements ProcessDAO {
	@PersistenceUnit
	private EntityManager em;
	
	@Id @Column(name="PROCESS_ID") 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long _id;
	@Basic @Column(name="NUMBER_OF_INSTANCES") private int _numInstances;
	@Basic @Column(name="PROCESS_KEY") private QName _processKey;
	@Basic @Column(name="PROCESS_TYPE") private QName _processType;
	@Basic @Column(name="GUID") private String _guid;
	@Version @Column(name="VERSION") private int _version;
	
	@OneToMany(targetEntity=ProcessInstanceDAOImpl.class,mappedBy="_process",fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<ProcessInstanceDAO> _instances = new ArrayList<ProcessInstanceDAO>();
	@OneToMany(fetch=FetchType.LAZY,cascade={CascadeType.ALL})
	private Collection<CorrelatorDAOImpl> _correlators = new ArrayList<CorrelatorDAOImpl>();
	@ManyToOne(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST})
	@Column(name="CONNECTION_ID")
	private BPELDAOConnectionImpl _connection;
	
	public ProcessDAOImpl() {}
	public ProcessDAOImpl(QName key, QName type, String guid, BPELDAOConnectionImpl connection) {
		_processKey = key;
		_processType = type;
		_connection = connection;
		_guid = guid;
	}
	
	public void addCorrelator(String correlator) {
		_correlators.add(new CorrelatorDAOImpl(correlator));
	}

	public ProcessInstanceDAO createInstance(
			CorrelatorDAO instantiatingCorrelator) {
		ProcessInstanceDAOImpl inst = new ProcessInstanceDAOImpl((CorrelatorDAOImpl)instantiatingCorrelator, this,_connection);
		_instances.add(inst);
		_numInstances++;
		
		return inst;
	}

	public void delete() {
		if ( _connection.getEntityManager() != null ) {
			_connection.getEntityManager().remove(this);
		}
	}

	public Collection<ProcessInstanceDAO> findInstance(CorrelationKey cckey) {
		Collection<ProcessInstanceDAO> ret = new ArrayList<ProcessInstanceDAO>();
		
		for (ProcessInstanceDAO pi : _instances) {
			scope_block:for (ScopeDAO s : pi.getScopes()) {
				for (CorrelationSetDAO c : s.getCorrelationSets()) {
					if (c.getValue().equals(cckey)) ret.add(pi);
					break scope_block; 
				}
			}
		}
		return ret;
	}

	public CorrelatorDAO getCorrelator(String correlatorId) {
		for ( CorrelatorDAO c : _correlators ) {
			if ( c.getCorrelatorId().equals(correlatorId) ) return c;
		}
		return null;
	}

	public ProcessInstanceDAO getInstance(Long iid) {
		for (ProcessInstanceDAO pi : _instances) {
			if ( pi.getInstanceId() == iid ) return pi;
		}
		return null;
	}

	public int getNumInstances() {
		return _numInstances;
	}

	public QName getProcessId() {
		return _processKey;
	}

	public QName getType() {
		return _processType;
	}

	public int getVersion() {
		return _version;
	}

	public void instanceCompleted(ProcessInstanceDAO instance) {
		// TODO Auto-generated method stub
	}

	public void removeRoutes(String routeId, ProcessInstanceDAO target) {
        for (CorrelatorDAO c : _correlators) {
            c.removeRoutes(routeId, target);
        }

	}
	public String getGuid() {
		return _guid;
	}

}
