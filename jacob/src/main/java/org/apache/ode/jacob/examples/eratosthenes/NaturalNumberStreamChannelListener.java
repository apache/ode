/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:03 UTC 2015
 * For Interface : org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStream
 */

package org.apache.ode.jacob.examples.eratosthenes;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

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

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStream.class);

    protected Logger log() { return __log; } 

    protected NaturalNumberStreamChannelListener(org.apache.ode.jacob.examples.eratosthenes.NaturalNumberStreamChannel channel) {
       super(channel);
    }
}
