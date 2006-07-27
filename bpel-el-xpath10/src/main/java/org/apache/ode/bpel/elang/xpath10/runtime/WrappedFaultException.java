/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.elang.xpath10.runtime;

import org.apache.ode.bpel.common.FaultException;

import org.jaxen.FunctionCallException;
import org.jaxen.UnresolvableException;

/**
 *  Wrap a fault in a jaxen exception
 */
public interface WrappedFaultException {
	public FaultException getFaultException();
	
	/**
	 * Jaxenized  {@link FaultException}; Jaxen requires us to throw only exceptions
	 * extending its {@link UnresolvableVariableException} so we comply.
	 */
	static class JaxenUnresolvableException extends UnresolvableException implements WrappedFaultException{
	  FaultException _cause;
	  public JaxenUnresolvableException(FaultException e) {
	  	super("var");
	    assert e != null;
	    initCause(e);
	    _cause = e;
	  }

	  public FaultException getFaultException() {
	    return _cause;
	  }
	}
	
	/**
	 * Jaxenized  {@link FaultException}; Jaxen requires us to throw only exceptions
	 * extending its {@link FunctionCallException} so we comply.
	 */
	static class JaxenFunctionException extends FunctionCallException implements WrappedFaultException{
	  FaultException _cause;
	  public JaxenFunctionException(FaultException e) {
	    super(e);
	    assert e != null;
	    initCause(e);
	    _cause = e;
	  }

	  public FaultException getFaultException() {
	    return _cause;
	  }
	}
}
