/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.TimerResponse
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.TimerResponse} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.TimerResponse
 * @see org.apache.ode.bpel.runtime.channels.TimerResponseChannel
 */
public abstract class TimerResponseChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.TimerResponseChannel>
    implements org.apache.ode.bpel.runtime.channels.TimerResponse
{

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.TimerResponse.class);

    protected Logger log() { return __log; } 

    protected TimerResponseChannelListener(org.apache.ode.bpel.runtime.channels.TimerResponseChannel channel) {
       super(channel);
    }
}
