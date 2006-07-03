package com.fs.pxe.sfwk.impl.endpoint;

import java.util.Map;

/**
 * Tries to express (even with an ugly name) the fact that the endpoint
 * can be either tranformed to a Map representation or initialized from
 * a Map. Used for endpoint conversion, to transform one endpoint type
 * into another (using Map as an intermediary format).
 */
public interface MapReducibleEndpoint {

  static final String ADDRESS = "address";
  static final String SESSION = "session";

  Map toMap();

  void fromMap(Map eprMap);
}
