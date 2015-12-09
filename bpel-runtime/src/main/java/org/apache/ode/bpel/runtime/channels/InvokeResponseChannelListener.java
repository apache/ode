/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.InvokeResponse
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.InvokeResponse.class);

    protected Logger log() { return __log; } 

    protected InvokeResponseChannelListener(org.apache.ode.bpel.runtime.channels.InvokeResponseChannel channel) {
       super(channel);
    }
}
