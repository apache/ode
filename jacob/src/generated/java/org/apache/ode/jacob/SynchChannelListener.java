/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * !!! DO NOT EDIT !!!!
 */
package org.apache.ode.jacob;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public abstract class SynchChannelListener
    extends org.apache.ode.jacob.ChannelListener<SynchChannel>
    implements Synch
 {
  private static final Log __log = LogFactory.getLog(SynchChannelListener.class);
  protected Log log() { return __log; }

  protected SynchChannelListener(
    SynchChannel channel) {
    super(channel);
  }

}
