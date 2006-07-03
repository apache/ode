package com.fs.pxe.sfwk.core;

/**
 * Specialized service endpoint for stateful services.
 */
public interface StatefulServiceEndpoint extends ServiceEndpoint {

  void setUrl(String url);

  String getSessionId();

  void setSessionId(String sessionId);

}
