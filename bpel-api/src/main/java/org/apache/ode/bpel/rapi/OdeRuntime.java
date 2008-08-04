package org.apache.ode.bpel.rapi;

import org.apache.ode.bpel.iapi.ProcessConf;
import org.apache.ode.bpel.common.FaultException;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

public interface OdeRuntime {
    
    void init(ProcessConf pconf);

    OdeRTInstance newInstance(Object state);

    Object getReplacementMap(QName processName);

    ProcessModel getModel();

    void clear();

    String extractProperty(Element msgData, PropertyAliasModel alias, String target) throws FaultException;
}
