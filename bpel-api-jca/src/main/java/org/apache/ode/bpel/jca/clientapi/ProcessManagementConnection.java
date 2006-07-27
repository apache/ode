package org.apache.ode.bpel.jca.clientapi;

import javax.resource.cci.Connection;

import org.apache.ode.bpel.pmapi.InstanceManagement;
import org.apache.ode.bpel.pmapi.ProcessManagement;

/**
 * JCA {@link javax.resource.cci.Connection} interface combining process and 
 * instance management.
 */
public interface ProcessManagementConnection 
  extends ProcessManagement, InstanceManagement, Connection 
{
  
}
