package org.apache.ode.bpel.iapi;

import org.apache.ode.bpel.evt.BpelEvent;

/**
 * Listener interface implemented by parties interested in the
 * 
 * @author mszefler
 *
 */
public interface BpelEventListener {
  void onEvent(BpelEvent bpelEvent);
}
