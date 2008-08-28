package org.apache.ode.bpel.rtrep.v2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.ode.bpel.rtrep.v2.channels.ReadWriteLockChannel;


/**
 * Data structure used to keep track of global (instance-level) data. 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class InstanceGlobals implements Serializable {
    
    /** Variable locks. Used by isolated scopes. */
    Map<OScope.Variable, ReadWriteLockChannel> _varLocks = new HashMap<OScope.Variable, ReadWriteLockChannel>();
}
