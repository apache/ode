/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:03 UTC 2015
 * For Interface : org.apache.ode.jacob.Synch
 */

package org.apache.ode.jacob;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.jacob.Synch} channel type. 
 * @see org.apache.ode.jacob.Synch
 * @see org.apache.ode.jacob.SynchChannel
 */
public abstract class SynchChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.jacob.SynchChannel>
    implements org.apache.ode.jacob.Synch
{

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.jacob.Synch.class);

    protected Logger log() { return __log; } 

    protected SynchChannelListener(org.apache.ode.jacob.SynchChannel channel) {
       super(channel);
    }
}
