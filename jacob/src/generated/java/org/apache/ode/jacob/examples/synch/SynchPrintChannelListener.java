/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * !!! DO NOT EDIT !!!!
 */
package org.apache.ode.jacob.examples.synch;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public abstract class SynchPrintChannelListener
    extends org.apache.ode.jacob.ChannelListener<SynchPrintChannel>
    implements SynchPrint
 {
  private static final Log __log = LogFactory.getLog(SynchPrintChannelListener.class);
  protected Log log() { return __log; }

  protected SynchPrintChannelListener(
    SynchPrintChannel channel) {
    super(channel);
  }

}
