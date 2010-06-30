/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.ReadWriteLock
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.ReadWriteLock} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.ReadWriteLock
 * @see org.apache.ode.bpel.runtime.channels.ReadWriteLockChannel
 */
public abstract class ReadWriteLockChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.ReadWriteLockChannel>
    implements org.apache.ode.bpel.runtime.channels.ReadWriteLock
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.ReadWriteLock.class);

    protected Log log() { return __log; } 

    protected ReadWriteLockChannelListener(org.apache.ode.bpel.runtime.channels.ReadWriteLockChannel channel) {
       super(channel);
    }
}
