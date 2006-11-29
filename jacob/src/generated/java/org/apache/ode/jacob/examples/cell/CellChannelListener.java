/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * !!! DO NOT EDIT !!!!
 */
package org.apache.ode.jacob.examples.cell;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public abstract class CellChannelListener
    extends org.apache.ode.jacob.ChannelListener<CellChannel>
    implements Cell
 {
  private static final Log __log = LogFactory.getLog(CellChannelListener.class);
  protected Log log() { return __log; }

  protected CellChannelListener(
    CellChannel channel) {
    super(channel);
  }

}
