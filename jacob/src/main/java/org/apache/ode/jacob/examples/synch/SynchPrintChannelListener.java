/*
 * SOURCE FILE GENERATATED BY JACOB CHANNEL CLASS GENERATOR
 * 
 *               !!! DO NOT EDIT !!!! 
 * 
 * Generated On  : Sat Dec 05 03:48:03 UTC 2015
 * For Interface : org.apache.ode.jacob.examples.synch.SynchPrint
 */

package org.apache.ode.jacob.examples.synch;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * An auto-generated channel listener abstract class for the 
 * {@link org.apache.ode.jacob.examples.synch.SynchPrint} channel type. 
 * @see org.apache.ode.jacob.examples.synch.SynchPrint
 * @see org.apache.ode.jacob.examples.synch.SynchPrintChannel
 */
public abstract class SynchPrintChannelListener
    extends org.apache.ode.jacob.ChannelListener<org.apache.ode.jacob.examples.synch.SynchPrintChannel>
    implements org.apache.ode.jacob.examples.synch.SynchPrint
{

    private static final Logger __log = LoggerFactory.getLogger(org.apache.ode.jacob.examples.synch.SynchPrint.class);

    protected Logger log() { return __log; } 

    protected SynchPrintChannelListener(org.apache.ode.jacob.examples.synch.SynchPrintChannel channel) {
       super(channel);
    }
}
