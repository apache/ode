/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.bpel.hobj;

import com.fs.pxe.daohib.hobj.HLargeData;
import com.fs.pxe.daohib.hobj.HObject;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Hibernate table representing a BPEL process (<em>not</em> a process instance).
 * @hibernate.class table="BPEL_PROCESS"
 *
 */
public class HProcess extends HObject{

  /** {@link HCorrelator}s for this process. */
  private Set<HCorrelator> _correlators = new HashSet<HCorrelator>();

  /** Instances of this BPEL process. */
  private Collection<HProcessInstance> _instances = new HashSet<HProcessInstance>();

  /** {@link HProcessProperty}s for this process. */
  private Set<HProcessProperty> _properties = new HashSet<HProcessProperty>();

  /** Events belonging to this BPEL process. */
  private Collection<HBpelEvent> _events = new HashSet<HBpelEvent>();

  /** Partnerlinks defined on this process */
  private Set<HPartnerLink> _plinks = new HashSet<HPartnerLink>();

  /** Message exchanges associated with this process. */
  private Set<HMessageExchange> _messageExchanges = new HashSet<HMessageExchange>();

  /** Simple name of the process. */
  private String _processId;

  /** User that deployed the process. */
	private String _deployer;
	
	/** Date of last deployment. */
	private Date _deployDate;
	
	/** Process name. */
	private String _type;

	/** Process version. */
	private int _version;

  /** Whether process is retired */
  private boolean _retired;

  private boolean _active;

  private String _deployURI;

  private HLargeData _compiledProcess;

  /**
   * @hibernate.set
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="PROCESS"
   * @hibernate.collection-one-to-many
   *   class="com.fs.pxe.daohib.bpel.hobj.HMessageExchange"
   */
  public Set<HMessageExchange> getMessageExchanges() {
    return _messageExchanges;
  }

  public void setMessageExchanges(Set<HMessageExchange> exchanges) {
    _messageExchanges = exchanges;
  }
  
  /**
   * @hibernate.set
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="PROCESS_ID"
   * @hibernate.collection-one-to-many
   *   class="com.fs.pxe.daohib.bpel.hobj.HCorrelator"
   */
	public Set<HCorrelator> getCorrelators() {
		return _correlators;
	}
  
	public void setCorrelators(Set<HCorrelator> correlators) {
		_correlators = correlators;
	}
  
  /**
   * @hibernate.bag
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="PROCESS_ID"
   * @hibernate.collection-one-to-many
   *  class="com.fs.pxe.daohib.bpel.hobj.HProcessInstance"
   */
	public Collection<HProcessInstance> getInstances() {
		return _instances;
	}

  public void setInstances(Collection<HProcessInstance> instances) {
		_instances = instances;
	}

  /**
   * @hibernate.set
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="PROCESS_ID"
   * @hibernate.collection-one-to-many
   *   class="com.fs.pxe.daohib.bpel.hobj.HProcessProperty"
   */
	public Set<HProcessProperty> getProperties() {
		return _properties;
	}

	public void setProperties(Set<HProcessProperty> properties) {
		_properties = properties;
	}

  /**
   * @hibernate.bag
   *  lazy="true"
   *  inverse="true"
   *  cascade="delete"
   * @hibernate.collection-key
   *  column="PID"
   * @hibernate.collection-one-to-many
   *  class="com.fs.pxe.daohib.bpel.hobj.HBpelEvent"
   */
  public Collection<HBpelEvent> getEvents() {
    return _events;
  }

  public void setEvents(Collection<HBpelEvent> events) {
    _events = events;
  }

  /**
   * Get the partner links values as deployed.
   *
   * @return {@link Set}&lt;{@link HPartnerLink}&gt; with variable values
   * @hibernate.set lazy="false" inverse="true" cascade="delete"
   * @hibernate.collection-key column="PROCESS"   
   * @hibernate.collection-one-to-many class="com.fs.pxe.daohib.bpel.hobj.HPartnerLink"
   */
  public Set<HPartnerLink> getDeploymentPartnerLinks() {
    return _plinks;
  }

  /**
   * Set the partner links as deployed.
   * @param partnerlinks
   */
  public void setDeploymentPartnerLinks(Set<HPartnerLink> partnerlinks) {
    _plinks = partnerlinks;
  }


  /**
   * 
   * @hibernate.property
   * @hibernate.column
   *  name="PROCID"
   *  not-null="true"
   *  unique="true"
   */
	public String getProcessId() {
		return _processId;
	}

  public void setProcessId(String processId) {
		_processId = processId;
	}

  /**
   * The user that deployed the process.
   * @hibernate.property
   *    column="deployer"
   */
	public String getDeployer() {
		return _deployer;
	}

	public void setDeployer(String deployer) {
		_deployer = deployer;
	}
	


	/**
	 * The date the process was deployed.
	 * @hibernate.property
	 *    column="deploydate"
	 */
	public Date getDeployDate() {
		return _deployDate;
	}



	public void setDeployDate(Date deployDate) {
		_deployDate = deployDate;
	}



	/**
	 * The type of the process (BPEL process definition name).
	 * @hibernate.property
	 *     column="type"
	 */
	public String getType() {
		return _type;
	}


	public void setType(String processName) {
		_type = processName;
	}

	/**
	 * The process version.
	 * @hibernate.property
	 *    column="version"
	 */
	public int getVersion() {
		return _version;
	}

	public void setVersion(int version) {
		_version = version;
	}

  /**
   * The process status.
   * @hibernate.property
   *    column="RETIRED"
   */
  public boolean isRetired() {
    return _retired;
  }
  
  public void setRetired(boolean retired) {
    this._retired = retired;
  }

  /**
   * The process status.
   * @hibernate.property
   *    column="ACTIVE"
   */
  public boolean isActive() {
    return _active;
  }
  
  public void setActive(boolean active) {
    _active = active;
  }

  public void setDeployURI(String uri) {
    _deployURI = uri;
  }

  /**
   * The URI of the depoloyment descriptor.
   * @hibernate.property
   *    column="DEPLOYURI"
   */
  public String getDeployURI() {
    return _deployURI;
  }

  /** @hibernate.many-to-one column="CBP" */
  public HLargeData getCompiledProcess() {
    return _compiledProcess;
  }

  public void setCompiledProcess(HLargeData compiledProcess) {
    _compiledProcess = compiledProcess;
  }
  
}
