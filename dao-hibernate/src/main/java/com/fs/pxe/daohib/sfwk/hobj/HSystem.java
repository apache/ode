/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk.hobj;

import com.fs.pxe.daohib.hobj.HLargeData;

import java.util.Collection;
import java.util.HashSet;

/**
 * Hibernate-managed table for keeping track of PXE systems.
 *
 * @hibernate.class
 *  table="PXE_SYSTEM"
 *
 * @hibernate.query name="HSystem.DeployedByName"
 *   query="from HSystem sys where sys.deployed=true and sys.systemName = :name"
 *
 */
public class HSystem {
	
  private Collection<HSfwkMessageExchange> _messageExchanges = new HashSet<HSfwkMessageExchange>();
  private String _systemName;
  private String _systemUUID;
  private HDomain _domain;
  private boolean _active;
  private boolean _deployed;
  private HLargeData _sar;
  private HLargeData _sdd;

  /**
   * @hibernate.many-to-one column="SAR_LDATA_ID" cascade="delete"
   */
  public HLargeData getSystemArchive() {
    return _sar;
  }
  public void setSystemArchive(HLargeData sarData) {
    _sar = sarData;
  }


  public HSystem() {
    super();
  }

  /**
   * The {@link HDomain} to which this system belongs.
   * @hibernate.many-to-one
   *    column="DOMAIN_ID"
   */
	public HDomain getDomain() {
		return _domain;
	}
  
	public void setDomain(HDomain domain) {
		_domain = domain;
	}

  /**
   * @hibernate.bag
   *   lazy="true"
   *   inverse="true"
   *   cascade="delete"
   * @hibernate.collection-key
   *  column="SYSTEM_ID"
   * @hibernate.collection-one-to-many
   *   class="com.fs.pxe.daohib.sfwk.hobj.HSfwkMessageExchange"
   */
	public Collection<HSfwkMessageExchange> getMessageExchanges() {
		return _messageExchanges;
	}

	public void setMessageExchanges(Collection<HSfwkMessageExchange> messageExchanges) {
		_messageExchanges = messageExchanges;
	}

  /**
   * System name.
   * @hibernate.property column="NAME" 
   */
	public String getSystemName() {
		return _systemName;
	}

	public void setSystemName(String systemName) {
		_systemName = systemName;
	}

  /**
   * @hibernate.many-to-one column="SDD_LDATA_ID" cascade="delete"
   */
  public HLargeData getSystemDeploymentDescriptor() {
    return _sdd;
  }

  public void setSystemDeploymentDescriptor(HLargeData sdd) {
    _sdd = sdd;
  }

  /**
   * Globally-unique system identifier.
   * @hibernate.id
   *    column="UUID"
   *    unique="true"
   *    generator-class="assigned"
   */
  public String getSystemUUID() {
    return _systemUUID;
	}

	public void setSystemUUID(String systemUUID) {
		_systemUUID = systemUUID;
	}


  /**
   * The system's "deployed" flag.
   * @hibernate.property column="DEPLOYED"
   */
  public boolean getDeployed() {
    return _deployed;
  }

  public void setDeployed(boolean deployed) {
    _deployed = deployed;
  }

  /**
   * The system's "active" flag.
   * @hibernate.property column="ACTIVE"
   */
  public boolean getActive() {
    return _active;
  }

  public void setActive(boolean active) {
    _active = active;
  }

}
