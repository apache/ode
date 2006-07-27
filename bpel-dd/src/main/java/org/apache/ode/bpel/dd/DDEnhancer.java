package org.apache.ode.bpel.dd;

import org.apache.ode.bpel.o.OProcess;

import javax.wsdl.Definition;

/**
 * Interface implemented to eventually update a deployment descriptor
 * to automatically add elements based on the process or the WSDL
 * definitions.
 * @see DDHandler
 */
public interface DDEnhancer {

  /**
   * Enhance a deployment descriptor. 
   * @param dd
   * @param process
   * @param wsdlDefs
   * @return true if the deployment descriptor has been modified by this enhancer
   */
  boolean enhance(TDeploymentDescriptor dd, OProcess process, Definition[] wsdlDefs) throws DDException;
}
