/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.TimerResponse
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.TimerResponse.class);

    protected Log log() { return __log; } 

    protected TimerResponseChannelListener(org.apache.ode.bpel.runtime.channels.TimerResponseChannel channel) {
       super(channel);
    }
}
