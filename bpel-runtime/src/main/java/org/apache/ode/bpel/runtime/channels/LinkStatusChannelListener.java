/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.LinkStatus
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.LinkStatus} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.LinkStatus
 * @see org.apache.ode.bpel.runtime.channels.LinkStatusChannel
 */
public abstract class LinkStatusChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.LinkStatusChannel>
    implements org.apache.ode.bpel.runtime.channels.LinkStatus
{

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.LinkStatus.class);

    protected Logger log() { return __log; } 

    protected LinkStatusChannelListener(org.apache.ode.bpel.runtime.channels.LinkStatusChannel channel) {
       super(channel);
    }
}
