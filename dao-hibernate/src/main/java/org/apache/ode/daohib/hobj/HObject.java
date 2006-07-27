/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.daohib.hobj;

import java.util.Date;

/**
 * Base class for Hibernate objects providing auto-generated key and
 * create timestamp.
 */
public class HObject {
	
  private Long _id;
  private Date _created;
  
  /** Constructor.	 */
	public HObject() {
		super();
	}

  /**
   * Auto-gnerated creation timestamp.
   * @hibernate.property
   *  column="INSERT_TIME"
   *  type="timestamp"
   */
  public Date getCreated() {
    return _created;
  }

  public void setCreated(Date created) {
    _created = created;
  }

  /**
   * Auto-generated primary key.
   * @hibernate.id
   *  generator-class="native"
   *  column="ID"
   */
  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }
}
