/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Thu Jun 24 22:32:44 EST 2010
 * For Interface : org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStream
 */

package org.apache.ode.jacob.examples.eratosthenes;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStream} channel type. 
 * @see org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStream
 * @see org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStreamChannel
 */
public abstract class NaturalNumberStreamChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStreamChannel>
    implements org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStream
{

    private static final Log __log = LogFactory.getLog(org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStream.class);

    protected Log log() { return __log; } 

    protected NaturalNumberStreamChannelListener(org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStreamChannel channel) {
       super(channel);
    }
}
