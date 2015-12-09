/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:07 UTC 2015
 * For Interface : org.apache.ode.bpel.runtime.channels.ParentScope
 */

package org.apache.ode.bpel.runtime.channels;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.bpel.runtime.channels.ParentScope} channel type. 
 * @see org.apache.ode.bpel.runtime.channels.ParentScope
 * @see org.apache.ode.bpel.runtime.channels.ParentScopeChannel
 */
public abstract class ParentScopeChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.bpel.runtime.channels.ParentScopeChannel>
    implements org.apache.ode.bpel.runtime.channels.ParentScope
{

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.bpel.runtime.channels.ParentScope.class);

    protected Logger log() { return __log; } 

    protected ParentScopeChannelListener(org.apache.ode.bpel.runtime.channels.ParentScopeChannel channel) {
       super(channel);
    }
}
