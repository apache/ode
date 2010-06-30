/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.InvokeResponse
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.InvokeResponse} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.InvokeResponse
 * @see org.apache.ode.bpel.runtime.channels.InvokeResponseChannel
 */
public abstract class InvokeResponseChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.InvokeResponseChannel>
    implements org.apache.ode.bpel.runtime.channels.InvokeResponse
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.InvokeResponse.class);

    protected Log log() { return __log; } 

    protected InvokeResponseChannelListener(org.apache.ode.bpel.runtime.channels.InvokeResponseChannel channel) {
       super(channel);
    }
}
