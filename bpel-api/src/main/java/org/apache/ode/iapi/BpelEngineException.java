package org.apache.ode.iapi;

/**
 * Exception thrown by the BPEL engine / BPEL server.
 */
public class BpelEngineException extends RuntimeException {

  public BpelEngineException(String msg, Exception e) {
    super(msg,e);
  }

  public BpelEngineException(Exception ex) {
    super(ex);
  }

  public BpelEngineException(String string) {
    super(string);
  }

}
