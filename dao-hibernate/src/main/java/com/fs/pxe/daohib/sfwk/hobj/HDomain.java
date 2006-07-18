/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.daohib.sfwk.hobj;

import java.util.HashSet;
import java.util.Set;

/**
 * Hibernate-managed table for keeping track of PXE domains.
 *
 * @hibernate.class
 *  table="PXE_DOMAIN"
 */
public class HDomain {
	
  private String _domainId;
  private Set<HSystem> _systems = new HashSet<HSystem>();

  /**
	 * 
	 */
	public HDomain() {}
  
  /**
   * @hibernate.id
   *    generator-class="assigned"
   *    column="id"
   */
	public String getDomainId() {
		return _domainId;
	}

	public void setDomainId(String domainId) {
		_domainId = domainId;
	}
  
   /**
   * @hibernate.set
   *   lazy="true"
   *   inverse="true"
   *   cascade="delete"
   * @hibernate.collection-key
   *  column="DOMAIN_ID"
   * @hibernate.collection-one-to-many
   *   class="com.fs.pxe.daohib.sfwk.hobj.HSystem"
   */
	public Set<HSystem> getSystems() {
		return _systems;
	}

  public void setSystems(Set<HSystem> systems) {
		_systems = systems;
	}
}
