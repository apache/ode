/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:35:42 EST 2010
 * For Interface : org.apache.ode.bpel.runtime.channels.ParentScope
 */

package org.apache.ode.bpel.runtime.channels;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

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

    private static final Log __log = LogFactory.getLog(org.apache.ode.bpel.runtime.channels.ParentScope.class);

    protected Log log() { return __log; } 

    protected ParentScopeChannelListener(org.apache.ode.bpel.runtime.channels.ParentScopeChannel channel) {
       super(channel);
    }
}
