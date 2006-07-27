/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import com.fs.jacob.Abstraction;
import com.fs.jacob.vpu.JacobVPU;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Base class extended by all BPEL-related abstractions. Provides methods for
 * manipulating the BPEL database, creating faults, and accessing the native
 * facilities.
 *
 * Created on Jan 12, 2004 at 5:41:27 PM.
 * @author Maciej Szefler
 */
public abstract class BpelAbstraction extends Abstraction {
  private static final Log __log = LogFactory.getLog(BpelAbstraction.class);

  protected BpelRuntimeContext getBpelRuntimeContext() {
    BpelRuntimeContext nativeApi = (BpelRuntimeContext) JacobVPU.activeJacobThread().getExtension(BpelRuntimeContext.class);
    assert nativeApi != null;
    return nativeApi;
  }

  protected Log log() {
    return __log;
  }
  
  protected final FaultData createFault(QName fault, Element faultMsg, OVarType faultType, OBase location){
  	return new FaultData(fault, faultMsg, faultType, location);
  }
  
	protected final FaultData createFault(QName fault, OBase location, String faultExplanation) {
    return new FaultData(fault, location,faultExplanation);
  }

  protected final FaultData createFault(QName fault, OBase location){
  	return createFault(fault, location, null);
  }
  

  protected Abstraction createChild(ActivityInfo childInfo, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    return new ACTIVITYGUARD(childInfo, scopeFrame, linkFrame);
  }

  protected void initializeCorrelation(CorrelationSetInstance cset, VariableInstance variable)
          throws FaultException {
    if (__log.isDebugEnabled()) {
      __log.debug("Initializing correlation set " + cset.declaration.name);
    }
    // if correlation set is already initialized,
    // then skip
    if (getBpelRuntimeContext().isCorrelationInitialized(cset)) {
      // if already set, we ignore
      if (__log.isDebugEnabled()) {
        __log.debug("OCorrelation set " + cset + " is already set: ignoring");
      }

      return;
    }

    String[] propNames = new String[cset.declaration.properties.size()];
    String[] propValues = new String[cset.declaration.properties.size()];

    for (int i = 0; i < cset.declaration.properties.size(); ++i) {
      OProcess.OProperty property = cset.declaration.properties.get(i);
      propValues[i] = getBpelRuntimeContext().readProperty(variable, property);
      propNames[i] = property.name.toString();
    }

    CorrelationKey ckeyVal = new CorrelationKey(cset.declaration.getId(), propValues);
    getBpelRuntimeContext().writeCorrelation(cset,ckeyVal);
  }
  
  protected long genMonotonic() {
    return getBpelRuntimeContext().genId();
  }
}
