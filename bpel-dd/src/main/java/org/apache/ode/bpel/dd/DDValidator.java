package org.apache.ode.bpel.dd;

import org.apache.ode.bpel.o.OProcess;

/**
 * Validates a specific part of a deployment descriptor.
 */
public interface DDValidator {

  void validate(TDeploymentDescriptor dd, OProcess process) throws DDValidationException;
}
