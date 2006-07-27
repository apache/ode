/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.Correlation;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OPickReceive;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import java.util.Collection;

/**
 * Base class for the {@link PickGenerator} and {@link ReceiveGenerator} classes.
 * Provides common functionality related to generating {@link org.apache.ode.bpel.o.OPickReceive.OnMessage}
 * objects.
 */
abstract class PickReceiveGenerator extends DefaultActivityGenerator {
  protected static final CommonCompilationMessages __cmsgsGeneral =
    MessageBundle.getMessages(CommonCompilationMessages.class);

  protected static final PickGeneratorMessages __cmsgs =
    MessageBundle.getMessages(PickGeneratorMessages.class);

  public OActivity newInstance(Activity src) {
    return new OPickReceive(_context.getOProcess());
  }


  /**
   * Compile an On-Message or Receive block.
   * @param varname name of variable to receive into
   * @param plink partner link to receive on
   * @param operation name of operation
   * @param portType optional portType
   * @param createInstance is this a start activity
   * @param correlations the correlations used
   * @return
   */
  protected OPickReceive.OnMessage compileOnMessage(String varname, String plink, String operation, String messageExchangeId, QName portType, boolean createInstance,
                                Collection<Correlation> correlations) {

    OPickReceive.OnMessage onMessage = new OPickReceive.OnMessage(_context.getOProcess());
    onMessage.partnerLink = _context.resolvePartnerLink(plink);
    onMessage.operation = _context.resolveMyRoleOperation(onMessage.partnerLink, operation);
    if (onMessage.operation.getInput() != null && onMessage.operation.getInput().getMessage() != null)
      onMessage.variable = _context.resolveMessageVariable(varname,onMessage.operation.getInput().getMessage().getQName());
    onMessage.messageExchangeId = messageExchangeId;

    if (portType != null && !portType.equals(onMessage.partnerLink.myRolePortType.getQName()))
      throw new CompilationException(__cmsgsGeneral.errPortTypeMismatch(portType, onMessage.partnerLink.myRolePortType.getQName()));

    if (createInstance)
      onMessage.partnerLink.addCreateInstanceOperation(onMessage.operation);

    for (Correlation correlation : correlations) {
      OScope.CorrelationSet cset = _context.resolveCorrelationSet(correlation.getCorrelationSet());

      switch (correlation.getInitiate()) {
        case Correlation.INITIATE_NO:
          if (createInstance)
            throw new CompilationException(__cmsgsGeneral.errUseOfUninitializedCorrelationSet(correlation.getCorrelationSet()));
          if (onMessage.matchCorrelation != null)
            throw new CompilationException(__cmsgs.errSecondNonInitiateCorrelationSet(correlation.getCorrelationSet()));
          onMessage.matchCorrelation = cset;
          onMessage.partnerLink.addCorrelationSetForOperation(onMessage.operation, cset);
          break;
        case Correlation.INITIATE_YES:
          onMessage.initCorrelations.add(cset);
          onMessage.partnerLink.addCorrelationSetForOperation(onMessage.operation, cset);
          break;
        case Correlation.INITIATE_RENDEZVOUS:
          throw new UnsupportedOperationException();
      }

      for (OProcess.OProperty property : cset.properties) {
        // Force resolution of alias, to make sure that we have one for this variable-property pair.
        _context.resolvePropertyAlias(onMessage.variable, property.name);
      }
    }

    if (!onMessage.partnerLink.hasMyRole()) {
      throw new CompilationException(__cmsgsGeneral.errNoMyRoleOnReceivePartnerLink(onMessage.partnerLink.getName()));
    }

    return onMessage;
  }

}
