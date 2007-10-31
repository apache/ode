package org.apache.ode.bpel.evar.jdbc;

import javax.xml.namespace.QName;

/**
 * Key for identifiying an external variable. 
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
class EVarId {
    final QName pid;
    final String varId;
    
    
    EVarId(QName pid, String varId) {
        this.pid = pid;
        this.varId = varId;
    }
    
    public boolean equals(Object o) {
        return ((EVarId)o).varId.equals(varId ) && ((EVarId)o).pid.equals(pid);
    }
    
    public int hashCode() {
        return varId.hashCode() ^ pid.hashCode(); 
    }
    
    public String toString() {
        return pid + "#" + varId;
    }
}
