package com.fs.pxe.bpel.o;

import java.net.URI;

/**
 * Compiled representation of an XSL sheet.
 */
public class OXslSheet extends OBase {

  private static final long serialVersionUID = 1L;

  public URI uri;
  public String sheetBody;

  public OXslSheet(OProcess owner) {
    super(owner);
  }

}
