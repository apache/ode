/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:32:44 EST 2010
 * For Interface : org.apache.ode.jacob.examples.cell.Cell
 */

package org.apache.ode.jacob.examples.cell;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.jacob.examples.cell.Cell} channel type. 
 * @see org.apache.ode.jacob.examples.cell.Cell
 * @see org.apache.ode.jacob.examples.cell.CellChannel
 */
public abstract class CellChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.jacob.examples.cell.CellChannel>
    implements org.apache.ode.jacob.examples.cell.Cell
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.jacob.examples.cell.Cell.class);

    protected Log log() { return __log; } 

    protected CellChannelListener(org.apache.ode.jacob.examples.cell.CellChannel channel) {
       super(channel);
    }
}
