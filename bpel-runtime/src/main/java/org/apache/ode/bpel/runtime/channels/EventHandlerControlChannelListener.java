/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.EventHandlerControl
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.EventHandlerControl.class);

    protected Log log() { return __log; } 

    protected EventHandlerControlChannelListener(org.apache.ode.bpel.runtime.channels.EventHandlerControlChannel channel) {
       super(channel);
    }
}
