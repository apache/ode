/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.Termination
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.Termination} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.Termination
 * @see org.apache.ode.bpel.runtime.channels.TerminationChannel
 */
public abstract class TerminationChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.TerminationChannel>
    implements org.apache.ode.bpel.runtime.channels.Termination
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.Termination.class);

    protected Log log() { return __log; } 

    protected TerminationChannelListener(org.apache.ode.bpel.runtime.channels.TerminationChannel channel) {
       super(channel);
    }
}
