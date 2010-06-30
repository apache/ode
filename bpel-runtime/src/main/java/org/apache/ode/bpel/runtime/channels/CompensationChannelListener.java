/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.Compensation
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.Compensation.class);

    protected Log log() { return __log; } 

    protected CompensationChannelListener(org.apache.ode.bpel.runtime.channels.CompensationChannel channel) {
       super(channel);
    }
}
