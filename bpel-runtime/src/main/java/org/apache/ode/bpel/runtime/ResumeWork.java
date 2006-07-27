/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.utils.ObjectPrinter;

import java.io.Serializable;

/**
 * Resumes execution of a bpel process
 *
 */
public class ResumeWork implements Serializable{
	static final long serialVersionUID = 1;

  private Long _pid;

	public ResumeWork(Long pid) {
		_pid = pid;
	}
  
  public Long getPID(){
  	return _pid;
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[]{"pid", _pid});
  }

}
