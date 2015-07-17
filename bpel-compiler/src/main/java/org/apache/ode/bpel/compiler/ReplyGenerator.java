/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.Correlation;
import org.apache.ode.bpel.obj.OActivity;
import org.apache.ode.bpel.obj.OMessageVarType;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OReply;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.utils.msg.MessageBundle;

import javax.wsdl.Fault;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Generates code for <code>&lt;reply&gt;</code> activities.
 */
class ReplyGenerator extends DefaultActivityGenerator  {

    private static final CommonCompilationMessages _cmsgsGeneral =
            MessageBundle.getMessages(CommonCompilationMessages.class);

    private static final ReplyGeneratorMessages __cmsgsLocal =
            MessageBundle.getMessages(ReplyGeneratorMessages.class);

    public OActivity newInstance(Activity src) {
        return new OReply(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity src) {
        org.apache.ode.bpel.compiler.bom.ReplyActivity replyDef = (org.apache.ode.bpel.compiler.bom.ReplyActivity) src;
        OReply oreply = (OReply) output;

        oreply.setIsFaultReply((replyDef.getFaultName()) != null);
        oreply.setPartnerLink(_context.resolvePartnerLink(replyDef.getPartnerLink()));
        oreply.setMessageExchangeId(replyDef.getMessageExchangeId());
        if (replyDef.getVariable() != null) {
            oreply.setVariable(_context.resolveVariable(replyDef.getVariable()));
            if (!(oreply.getVariable().getType() instanceof OMessageVarType))
                throw new CompilationException(_cmsgsGeneral.errMessageVariableRequired(oreply.getVariable().getName()));
        }

        if (oreply.getPartnerLink().getMyRolePortType() == null)
            throw new CompilationException(_cmsgsGeneral.errPartnerLinkDoesNotDeclareMyRole(oreply.getPartnerLink().getName()));
        // The portType on the reply is not necessary, so we check its validty only when present.
        if (replyDef.getPortType() != null && !oreply.getPartnerLink().getMyRolePortType().getQName().equals(replyDef.getPortType()))
            throw new CompilationException(_cmsgsGeneral.errPortTypeMismatch(replyDef.getPortType(),oreply.getPartnerLink().getMyRolePortType().getQName()));

        oreply.setOperation(_context.resolveMyRoleOperation(oreply.getPartnerLink(), replyDef.getOperation()));
        if (oreply.getOperation().getOutput() == null)
            throw new CompilationException(_cmsgsGeneral.errTwoWayOperationExpected(oreply.getOperation().getName()));

        if (oreply.isIsFaultReply()) {
            Fault flt = null;
            if (replyDef.getFaultName().getNamespaceURI().equals(oreply.getPartnerLink().getMyRolePortType().getQName().getNamespaceURI()))
                flt = oreply.getOperation().getFault(replyDef.getFaultName().getLocalPart());
            if (flt == null)
                throw new CompilationException(__cmsgsLocal.errUndeclaredFault(replyDef.getFaultName().getLocalPart(), oreply.getOperation().getName()));
            if (oreply.getVariable() != null && !((OMessageVarType)oreply.getVariable().getType()).getMessageType().equals(flt.getMessage().getQName()))
                throw new CompilationException(_cmsgsGeneral.errVariableTypeMismatch(oreply.getVariable().getName(), flt.getMessage().getQName(), ((OMessageVarType)oreply.getVariable().getType()).getMessageType()));
            oreply.setFault(replyDef.getFaultName());
        } else /* !oreply.isFaultReply */ {
            assert oreply.getFault() == null;
            if (oreply.getVariable() == null)
                throw new CompilationException(__cmsgsLocal.errOutputVariableMustBeSpecified());
            if (!((OMessageVarType)oreply.getVariable().getType()).getMessageType().equals(oreply.getOperation().getOutput().getMessage().getQName()))
                throw new CompilationException(_cmsgsGeneral.errVariableTypeMismatch(oreply.getVariable().getName(), oreply.getOperation().getOutput().getMessage().getQName(),((OMessageVarType)oreply.getVariable().getType()).getMessageType()));
        }

        Set<String> csetNames = new HashSet<String>(); // prevents duplicate cset in on one set of correlations
        for (Correlation correlation  : replyDef.getCorrelations()) {
            if( csetNames.contains(correlation.getCorrelationSet() ) ) {
                throw new CompilationException(_cmsgsGeneral.errDuplicateUseCorrelationSet(correlation
                        .getCorrelationSet()));
            }

            OScope.CorrelationSet cset = _context.resolveCorrelationSet(correlation.getCorrelationSet());

            switch (correlation.getInitiate()) {
                case UNSET:
                case NO:
                    oreply.getAssertCorrelations().add(cset);
                    break;
                case YES:
                    oreply.getInitCorrelations().add(cset);
                    break;
                case JOIN:
                    cset.setHasJoinUseCases(true);
                    oreply.getJoinCorrelations().add(cset);
                    break;
                default:
                    // TODO: Make error for this.
                    throw new AssertionError();
            }

            for (Iterator<OProcess.OProperty> j = cset.getProperties().iterator(); j.hasNext(); ) {
                OProcess.OProperty property = j.next();
                // Force resolution of alias, to make sure that we have one for this variable-property pair.
                _context.resolvePropertyAlias(oreply.getVariable(), property.getName());
            }

            csetNames.add(correlation.getCorrelationSet());
        }
    }
}
