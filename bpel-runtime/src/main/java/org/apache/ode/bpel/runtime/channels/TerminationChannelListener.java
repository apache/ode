/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.Termination
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.Termination.class);

    protected Logger log() { return __log; } 

    protected TerminationChannelListener(org.apache.ode.bpel.runtime.channels.TerminationChannel channel) {
       super(channel);
    }
}
