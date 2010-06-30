/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.PickResponse
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.PickResponse} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.PickResponse
 * @see org.apache.ode.bpel.runtime.channels.PickResponseChannel
 */
public abstract class PickResponseChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.PickResponseChannel>
    implements org.apache.ode.bpel.runtime.channels.PickResponse
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.PickResponse.class);

    protected Log log() { return __log; } 

    protected PickResponseChannelListener(org.apache.ode.bpel.runtime.channels.PickResponseChannel channel) {
       super(channel);
    }
}
