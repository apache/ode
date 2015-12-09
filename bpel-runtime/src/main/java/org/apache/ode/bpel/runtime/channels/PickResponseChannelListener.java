/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.PickResponse
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.PickResponse.class);

    protected Logger log() { return __log; } 

    protected PickResponseChannelListener(org.apache.ode.bpel.runtime.channels.PickResponseChannel channel) {
       super(channel);
    }
}
