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
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OReply;
import org.apache.ode.bpel.o.OScope;
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

        oreply.isFaultReply = replyDef.getFaultName() != null;
        oreply.partnerLink = _context.resolvePartnerLink(replyDef.getPartnerLink());
        oreply.messageExchangeId = replyDef.getMessageExchangeId();
        if (replyDef.getVariable() != null) {
            oreply.variable = _context.resolveVariable(replyDef.getVariable());
            if (!(oreply.variable.type instanceof OMessageVarType))
                throw new CompilationException(_cmsgsGeneral.errMessageVariableRequired(oreply.variable.name));
        }

        if (oreply.partnerLink.myRolePortType == null)
            throw new CompilationException(_cmsgsGeneral.errPartnerLinkDoesNotDeclareMyRole(oreply.partnerLink.getName()));
        // The portType on the reply is not necessary, so we check its validty only when present.
        if (replyDef.getPortType() != null && !oreply.partnerLink.myRolePortType.getQName().equals(replyDef.getPortType()))
            throw new CompilationException(_cmsgsGeneral.errPortTypeMismatch(replyDef.getPortType(),oreply.partnerLink.myRolePortType.getQName()));

        oreply.operation = _context.resolveMyRoleOperation(oreply.partnerLink, replyDef.getOperation());
        if (oreply.operation.getOutput() == null)
            throw new CompilationException(_cmsgsGeneral.errTwoWayOperationExpected(oreply.operation.getName()));

        if (oreply.isFaultReply) {
            Fault flt = null;
            if (replyDef.getFaultName().getNamespaceURI().equals(oreply.partnerLink.myRolePortType.getQName().getNamespaceURI()))
                flt = oreply.operation.getFault(replyDef.getFaultName().getLocalPart());
            if (flt == null)
                throw new CompilationException(__cmsgsLocal.errUndeclaredFault(replyDef.getFaultName().getLocalPart(), oreply.operation.getName()));
            if (oreply.variable != null && !((OMessageVarType)oreply.variable.type).messageType.equals(flt.getMessage().getQName()))
                throw new CompilationException(_cmsgsGeneral.errVariableTypeMismatch(oreply.variable.name, flt.getMessage().getQName(), ((OMessageVarType)oreply.variable.type).messageType));
            oreply.fault = replyDef.getFaultName();
        } else /* !oreply.isFaultReply */ {
            assert oreply.fault == null;
            if (oreply.variable == null)
                throw new CompilationException(__cmsgsLocal.errOutputVariableMustBeSpecified());
            if (!((OMessageVarType)oreply.variable.type).messageType.equals(oreply.operation.getOutput().getMessage().getQName()))
                throw new CompilationException(_cmsgsGeneral.errVariableTypeMismatch(oreply.variable.name, oreply.operation.getOutput().getMessage().getQName(),((OMessageVarType)oreply.variable.type).messageType));
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
                    oreply.assertCorrelations.add(cset);
                    break;
                case YES:
                    oreply.initCorrelations.add(cset);
                    break;
                case JOIN:
                    cset.hasJoinUseCases = true;
                    oreply.joinCorrelations.add(cset);
                    break;
                default:
                    // TODO: Make error for this.
                    throw new AssertionError();
            }

            for (Iterator<OProcess.OProperty> j = cset.properties.iterator(); j.hasNext(); ) {
                OProcess.OProperty property = j.next();
                // Force resolution of alias, to make sure that we have one for this variable-property pair.
                _context.resolvePropertyAlias(oreply.variable, property.name);
            }

            csetNames.add(correlation.getCorrelationSet());
        }
    }
}
