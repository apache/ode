/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:32:44 EST 2010
 * For Interface : org.apache.ode.jacob.examples.synch.SynchPrint
 */

package org.apache.ode.jacob.examples.synch;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.jacob.examples.synch.SynchPrint} channel type. 
 * @see org.apache.ode.jacob.examples.synch.SynchPrint
 * @see org.apache.ode.jacob.examples.synch.SynchPrintChannel
 */
public abstract class SynchPrintChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.jacob.examples.synch.SynchPrintChannel>
    implements org.apache.ode.jacob.examples.synch.SynchPrint
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.jacob.examples.synch.SynchPrint.class);

    protected Log log() { return __log; } 

    protected SynchPrintChannelListener(org.apache.ode.jacob.examples.synch.SynchPrintChannel channel) {
       super(channel);
    }
}
