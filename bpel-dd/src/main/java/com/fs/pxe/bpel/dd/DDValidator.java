package com.fs.pxe.bpel.dd;

import com.fs.pxe.bpel.o.OProcess;

/**
 * Validates a specific part of a deployment descriptor.
 */
public interface DDValidator {

  void validate(TDeploymentDescriptor dd, OProcess process) throws DDValidationException;
}
