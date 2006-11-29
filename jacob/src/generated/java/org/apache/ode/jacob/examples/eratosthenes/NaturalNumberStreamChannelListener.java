/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * !!! DO NOT EDIT !!!!
 */
package org.apache.ode.jacob.examples.eratosthenes;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public abstract class NaturalNumberStreamChannelListener
    extends org.apache.ode.jacob.ChannelListener<NaturalNumberStreamChannel>
    implements NaturalNumberStream
 {
  private static final Log __log = LogFactory.getLog(NaturalNumberStreamChannelListener.class);
  protected Log log() { return __log; }

  protected NaturalNumberStreamChannelListener(
    NaturalNumberStreamChannel channel) {
    super(channel);
  }

}
