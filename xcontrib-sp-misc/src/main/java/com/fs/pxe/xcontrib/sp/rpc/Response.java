/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.xcontrib.sp.rpc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A service invocation response: encapsulates the response (parts) of a service
 * invocation.
 */
public class Response implements Serializable {

  private Map<String, String> _partData = new HashMap<String, String>();

  Response() {
    super();
  }

  public void setPartData(String partName, String data) {
    assert partName != null;
    assert data != null;
    _partData.put(partName, data);
  }

  public String getPartData(String partName) {
    return _partData.get(partName);
  }
}
