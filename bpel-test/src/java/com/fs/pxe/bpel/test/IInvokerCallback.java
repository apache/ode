/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.test;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IInvokerCallback extends Remote{
  
  public void requestResponse(String id, String data) throws RemoteException;
  
  public Response invokeResponse(String partnerLink, String op) throws RemoteException;
  
  public void requestFailed(String cid,String error) throws RemoteException;

  public static class Response implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    public Response(String fault, String data){
    	this.faultName = fault;
      this.data = data;
    }
  	final String faultName; // may be null
    final String data;
  }

}
