/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.mngmt;

import com.fs.pxe.tools.ExecutionException;

public abstract class SystemCommand extends JmxCommand {

  public void validate() throws ExecutionException {
    String name = getSystemName();

    if (name == null) {
      throw new ExecutionException(__msgs.msgNameOrUuidRequired());
    }

    if (getDomain() == null) {
      if (getDomainUuid() == null) {
        throw new ExecutionException(__msgs.msgNoDomainFound());
      }
      else {
        throw new ExecutionException(__msgs.msgNoDomainFound(getDomainUuid()));
      }
    }

    if (getSystem() == null) {
      throw new ExecutionException(__msgs.msgNoSuchSystem(name, getDomainUuid()));
    }
  }
}
