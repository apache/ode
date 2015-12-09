/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.Compensation
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.Compensation} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.Compensation
 * @see org.apache.ode.bpel.runtime.channels.CompensationChannel
 */
public abstract class CompensationChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.CompensationChannel>
    implements org.apache.ode.bpel.runtime.channels.Compensation
{

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.Compensation.class);

    protected Logger log() { return __log; } 

    protected CompensationChannelListener(org.apache.ode.bpel.runtime.channels.CompensationChannel channel) {
       super(channel);
    }
}
