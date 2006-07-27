package com.fs.pxe.bpel.jca.clientapi;

import javax.resource.cci.Connection;

import com.fs.pxe.bpel.pmapi.InstanceManagement;
import com.fs.pxe.bpel.pmapi.ProcessManagement;

/**
 * JCA {@link javax.resource.cci.Connection} interface combining process and 
 * instance management.
 */
public interface ProcessManagementConnection 
  extends ProcessManagement, InstanceManagement, Connection 
{
  
}
