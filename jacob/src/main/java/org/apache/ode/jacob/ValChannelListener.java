/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:32:44 EST 2010
 * For Interface : org.apache.ode.jacob.Val
 */

package org.apache.ode.jacob;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.jacob.Val} channel type. 
 * @see org.apache.ode.jacob.Val
 * @see org.apache.ode.jacob.ValChannel
 */
public abstract class ValChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.jacob.ValChannel>
    implements org.apache.ode.jacob.Val
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.jacob.Val.class);

    protected Log log() { return __log; } 

    protected ValChannelListener(org.apache.ode.jacob.ValChannel channel) {
       super(channel);
    }
}
