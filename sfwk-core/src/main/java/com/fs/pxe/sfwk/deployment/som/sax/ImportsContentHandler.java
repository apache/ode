/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.Port;

class ImportsContentHandler extends PortsContentHandler {

  public ImportsContentHandler(ServiceContentHandler sch) {
    super(sch);
  }

  public void addPort(Port p) {
    _sch.getService().addImportedPort(p);
  }
}
