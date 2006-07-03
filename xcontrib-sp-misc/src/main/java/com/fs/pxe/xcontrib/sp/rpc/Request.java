/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.xcontrib.sp.rpc;

import java.io.Serializable;

/**
 * A web service invocation request.  In addition to encapsulating the
 * pperation name and operation parameters, objects of this class carry
 * destination information  consisting of system name, service name, and
 * port name.
 */
public class Request extends Response implements Serializable{
	
  private String _system, _svc, _port, _operation;

	public Request(String systemName, String svcName, String portName, String operation) {
    assert systemName != null;
    assert svcName != null;
    assert operation != null;
		_system = systemName;
    _svc = svcName;
    _port = portName;
    _operation = operation;
	}
  
  public String getOperation(){
  	return _operation;
  }
	public String getPort() {
		return _port;
	}
	public String getService() {
		return _svc;
	}
	public String getSystem() {
		return _system;
	}
  
  
  public String toString(){
  	return new StringBuffer("NativeRequest[ ")
            .append(_system + ",")
            .append(_svc + ",")
            .append(_port + ",")
            .append(_operation + "]").toString();
  }
}
