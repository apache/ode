/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.EventHandlerControl
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.EventHandlerControl} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.EventHandlerControl
 * @see org.apache.ode.bpel.runtime.channels.EventHandlerControlChannel
 */
public abstract class EventHandlerControlChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.EventHandlerControlChannel>
    implements org.apache.ode.bpel.runtime.channels.EventHandlerControl
{

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.EventHandlerControl.class);

    protected Logger log() { return __log; } 

    protected EventHandlerControlChannelListener(org.apache.ode.bpel.runtime.channels.EventHandlerControlChannel channel) {
       super(channel);
    }
}
