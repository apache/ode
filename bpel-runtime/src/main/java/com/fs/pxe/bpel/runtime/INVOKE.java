/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.common.FaultException;
import com.fs.pxe.bpel.o.OInvoke;
import com.fs.pxe.bpel.o.OScope;
import com.fs.pxe.bpel.runtime.channels.FaultData;
import com.fs.pxe.bpel.runtime.channels.InvokeResponseChannel;
import com.fs.pxe.bpel.runtime.channels.InvokeResponseML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;

/**
 * Abstraction that performs the work of the <code>invoke</code> activity.
 */
public class INVOKE extends ACTIVITY {
  private static final long serialVersionUID = 992248281026821783L;

  private OInvoke _oinvoke;

  public INVOKE(ActivityInfo self, ScopeFrame scopeFrame, LinkFrame linkFrame) {
    super(self, scopeFrame, linkFrame);
    _oinvoke = (OInvoke) _self.o;
  }

  public final void self() {
    Element outboundMsg;
    try {
      outboundMsg = setupOutbound(_oinvoke, _oinvoke.initCorrelationsInput);
    } catch (FaultException e) {
      FaultData fault = createFault(e.getQName(), _oinvoke);
      _self.parent.completed(fault, CompensationHandler.emptySet());
      return;
    }

    // if there is no output variable, then this is a one-way invoke
    boolean isTwoWay = _oinvoke.outputVar != null;

    try {
      if (!isTwoWay) {
        FaultData faultData = null;
        getBpelRuntimeContext().invoke(
            _scopeFrame.resolve(_oinvoke.partnerLink),
            _oinvoke.operation,
            outboundMsg, 
            null);

        _self.parent.completed(faultData, CompensationHandler.emptySet());

      } else /* two-way */{
        final VariableInstance outputVar = _scopeFrame
            .resolve(_oinvoke.outputVar);
        InvokeResponseChannel invokeResponseChannel = newChannel(InvokeResponseChannel.class);

        final String mexId = getBpelRuntimeContext().invoke(
            _scopeFrame.resolve(_oinvoke.partnerLink), _oinvoke.operation,
            outboundMsg, 
            invokeResponseChannel);

        object(new InvokeResponseML(invokeResponseChannel) {
          private static final long serialVersionUID = 4496880438819196765L;

          public void onResponse() {
            // we don't have to write variable data -> this already
            // happened in the nativeAPI impl
            FaultData fault = null;

            try {
              Element response = getBpelRuntimeContext().getPartnerResponse(mexId);
              getBpelRuntimeContext().initializeVariable(outputVar, response);
            } catch (Exception ex) {
              // TODO: Better error handling
              throw new RuntimeException(ex);
            }

            try {
              for (OScope.CorrelationSet anInitCorrelationsOutput : _oinvoke.initCorrelationsOutput) {
                initializeCorrelation(_scopeFrame.resolve(anInitCorrelationsOutput), outputVar);
              }
              if (_oinvoke.partnerLink.hasPartnerRole()) {
                // Trying to initialize partner epr based on a message-provided epr/session.
                Node fromEpr = getBpelRuntimeContext().getSourceEPR(mexId);
                if (fromEpr != null) {
                  getBpelRuntimeContext().writeEndpointReference(
                          _scopeFrame.resolve(_oinvoke.partnerLink), (Element) fromEpr);
                }
              }
            } catch (FaultException e) {
              fault = createFault(e.getQName(), _oinvoke);
            }

            // TODO update output variable with data from non-initiate
            // correlation sets
            _self.parent.completed(fault, CompensationHandler.emptySet());
          }

          public void onFault() {
            QName faultName = getBpelRuntimeContext().getPartnerFault(mexId);
            Element msg = getBpelRuntimeContext().getPartnerResponse(mexId);
            QName msgType = getBpelRuntimeContext().getPartnerResponseType(
                mexId);
            FaultData fault = createFault(faultName, msg,
                _oinvoke.getOwner().messageTypes.get(msgType), _self.o);
            _self.parent.completed(fault, CompensationHandler.emptySet());
          }

          public void onFailure() {
            // This indicates a communication failure. We don't throw a fault,
            // because there is no fault, instead we'll re-incarnate the invoke
            // and ask the runtime to terminate us: this will allow the sys
            // admin to resume the process.
            instance(INVOKE.this);
            getBpelRuntimeContext().terminate();
          }
        });
      }
    } catch (FaultException fault) {
      FaultData faultData = createFault(fault.getQName(), _oinvoke, fault
          .getMessage());
      _self.parent.completed(faultData, CompensationHandler.emptySet());
    }
  }

  private Element setupOutbound(OInvoke oinvoke,
      Collection<OScope.CorrelationSet> outboundInitiations)
      throws FaultException {
    if (outboundInitiations.size() > 0) {
      for (OScope.CorrelationSet c : outboundInitiations) {
        initializeCorrelation(_scopeFrame.resolve(c), _scopeFrame.resolve(oinvoke.inputVar));
      }
    }

    Node outboundMsg = getBpelRuntimeContext().fetchVariableData(
        _scopeFrame.resolve(oinvoke.inputVar), false);

    // TODO outbound message should be updated with non-initiate correlation
    // sets
    assert outboundMsg instanceof Element;

    return (Element) outboundMsg;
  }

}
