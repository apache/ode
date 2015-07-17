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
import org.apache.ode.bpel.compiler.bom.InvokeActivity;
import org.apache.ode.bpel.obj.OActivity;
import org.apache.ode.bpel.obj.OInvoke;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;

import javax.wsdl.OperationType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates code for <code>&lt;invoke&gt;</code> activities.
 */
class InvokeGenerator extends DefaultActivityGenerator {
    private static final CommonCompilationMessages __cmsgsGeneral = MessageBundle.getMessages(CommonCompilationMessages.class);

    private static final InvokeGeneratorMessages __imsgs = MessageBundle.getMessages(InvokeGeneratorMessages.class);

    public OActivity newInstance(Activity src) {
        return new OInvoke(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity output, Activity srcx) {
        InvokeActivity src = (InvokeActivity) srcx;
        final OInvoke oinvoke = (OInvoke) output;

        oinvoke.setPartnerLink(_context.resolvePartnerLink(src.getPartnerLink()));
        oinvoke.setOperation(_context.resolvePartnerRoleOperation(oinvoke.getPartnerLink(), src.getOperation()));
        assert oinvoke.getOperation().getInput() != null; // ensured by
        // resolvePartnerRoleOperation
        assert oinvoke.getOperation().getInput().getMessage() != null; // ensured
        // by
        // resolvePartnerRoleOperation
        // TODO: Add portType checking if specified by user
        // if (portType != null &&
        // !portType.equals(onMessage.partnerLink.myRolePortType.getQName()))
        // throw new CompilationException(CMSGSG.errPortTypeMismatch(portType,
        // onMessage.partnerLink.myRolePortType.getQName()));
        if (oinvoke.getOperation().getInput() != null && oinvoke.getOperation().getInput().getMessage() != null) {
            // Input var can be omitted if input message has no part
            if (oinvoke.getOperation().getInput().getMessage().getParts().size() > 0) {
                if (src.getInputVar() == null)
                    throw new CompilationException(__imsgs.errInvokeNoInputMessageForInputOp(oinvoke.getOperation().getName()));
                oinvoke.setInputVar(_context.resolveMessageVariable(src.getInputVar(), oinvoke.getOperation().getInput().getMessage().getQName()));
            }
        }
        if (oinvoke.getOperation().getOutput() != null && oinvoke.getOperation().getOutput().getMessage() != null) {
            if (src.getOutputVar() == null)
                throw new CompilationException(__imsgs.errInvokeNoOutputMessageForOutputOp(oinvoke.getOperation().getName()));
            oinvoke.setOutputVar(_context.resolveMessageVariable(src.getOutputVar(), oinvoke.getOperation().getOutput().getMessage().getQName()));
        }
        List<Correlation> correlations = src.getCorrelations();
        List<Correlation> incorrelations = CollectionsX.filter(new ArrayList<Correlation>(), correlations,
                new MemberOfFunction<Correlation>() {
                    @Override
                    public boolean isMember(Correlation o) {
                        return o.getPattern() == Correlation.CorrelationPattern.IN;
                    }
                });
        List<Correlation> outcorrelations = CollectionsX.filter(new ArrayList<Correlation>(), correlations,
                new MemberOfFunction<Correlation>() {
                    @Override
                    public boolean isMember(Correlation o) {
                        return (o.getPattern() == Correlation.CorrelationPattern.OUT)
                                 || (o.getPattern()== Correlation.CorrelationPattern.UNSET && oinvoke.getOperation().getStyle()== OperationType.ONE_WAY );
                    }
                });

        List<Correlation> inoutcorrelations = CollectionsX.filter(new ArrayList<Correlation>(), correlations,
                new MemberOfFunction<Correlation>() {
                    @Override
                    public boolean isMember(Correlation o) {
                        return o.getPattern() == Correlation.CorrelationPattern.INOUT;
                    }
                });

        if (oinvoke.getInputVar() != null) {
            doCorrelations(outcorrelations, oinvoke.getInputVar(), oinvoke.getAssertCorrelationsInput(),
                    oinvoke.getInitCorrelationsInput(), oinvoke.getJoinCorrelationsInput());
            doCorrelations(inoutcorrelations, oinvoke.getInputVar(), oinvoke.getAssertCorrelationsInput(),
                    oinvoke.getInitCorrelationsInput(), oinvoke.getJoinCorrelationsInput());
        }
        if (oinvoke.getOutputVar() != null) {
            doCorrelations(incorrelations, oinvoke.getOutputVar(),
                    oinvoke.getAssertCorrelationsOutput(), oinvoke.getInitCorrelationsOutput(), oinvoke.getJoinCorrelationsOutput());
            doCorrelations(inoutcorrelations, oinvoke.getOutputVar(),
                    oinvoke.getAssertCorrelationsOutput(), oinvoke.getInitCorrelationsOutput(), oinvoke.getJoinCorrelationsOutput());
        }

    }

    private void doCorrelations(List<Correlation> correlations, OScope.Variable var,
            Collection<OScope.CorrelationSet> assertCorrelations,
            Collection<OScope.CorrelationSet> initCorrelations,
            Collection<OScope.CorrelationSet> joinCorrelations) {
        Set<String> csetNames = new HashSet<String>(); // prevents duplicate cset in on one set of correlations
        for (Correlation correlation : correlations) {
            if( csetNames.contains(correlation.getCorrelationSet() ) ) {
                throw new CompilationException(__cmsgsGeneral.errDuplicateUseCorrelationSet(correlation
                        .getCorrelationSet()));
            }

            OScope.CorrelationSet cset = _context.resolveCorrelationSet(correlation.getCorrelationSet());
            switch (correlation.getInitiate()) {
            case NO:
                assertCorrelations.add(cset);
                break;
            case YES:
                initCorrelations.add(cset);
                break;
            case JOIN:
                cset.setHasJoinUseCases(true);
                joinCorrelations.add(cset);
            }
            for (OProcess.OProperty property : cset.getProperties()) {
                // Force resolution of alias, to make sure that we have one for
                // this variable-property pair.
                try {
                    _context.resolvePropertyAlias(var, property.getName());
                } catch (CompilationException ce) {
                    if (ce.getCompilationMessage().source == null) {
                        ce.getCompilationMessage().source = correlation;
                    }
                    throw ce;
                }
                // onMessage.
            }
            csetNames.add(correlation.getCorrelationSet());
        }
    }
}
