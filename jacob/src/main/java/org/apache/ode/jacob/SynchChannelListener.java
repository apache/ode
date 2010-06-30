/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:32:44 EST 2010
 * For Interface : org.apache.ode.jacob.Synch
 */

package org.apache.ode.jacob;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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

    private static final Log __log = LogFactory.getLog(org.apache.ode.jacob.Synch.class);

    protected Log log() { return __log; } 

    protected SynchChannelListener(org.apache.ode.jacob.SynchChannel channel) {
       super(channel);
    }
}
