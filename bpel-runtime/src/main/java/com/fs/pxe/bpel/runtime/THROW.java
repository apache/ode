/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.o.OThrow;
import com.fs.pxe.bpel.runtime.channels.FaultData;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Throw BPEL fault activity.
 */
class THROW extends ACTIVITY {
	private static final long serialVersionUID = 1L;

	private OThrow _othrow;

  public THROW(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
    _othrow = (OThrow) self.o;
  }

  public void self() {
    FaultData fault = null;
    if(_othrow.faultVariable != null){
			try {
				Node faultVariable = getBpelRuntimeContext().fetchVariableData(_scopeFrame.resolve(_othrow.faultVariable), false);
        fault = createFault(_othrow.faultName, (Element)faultVariable,_othrow.faultVariable.type,_othrow);
			} catch (FaultException e) {
        // deal with this as a fault (just not the one we hoped for)
				fault = createFault(e.getQName(), _othrow);
			}
    }else{
    	fault = createFault(_othrow.faultName, _othrow);
    }

    _self.parent.completed(fault, CompensationHandler.emptySet());
  }
}
