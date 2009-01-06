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
import org.apache.ode.bpel.o.OPickReceive;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for the {@link PickGenerator} and {@link ReceiveGenerator}
 * classes. Provides common functionality related to generating
 * {@link org.apache.ode.bpel.o.OPickReceive.OnMessage} objects.
 */
abstract class PickReceiveGenerator extends DefaultActivityGenerator {
    protected static final CommonCompilationMessages __cmsgsGeneral = MessageBundle
            .getMessages(CommonCompilationMessages.class);

    protected static final PickGeneratorMessages __cmsgs = MessageBundle.getMessages(PickGeneratorMessages.class);

    public OActivity newInstance(Activity src) {
        return new OPickReceive(_context.getOProcess(), _context.getCurrent());
    }

    /**
     * Compile an On-Message or Receive block.
     * 
     * @param varname
     *            name of variable to receive into
     * @param plink
     *            partner link to receive on
     * @param operation
     *            name of operation
     * @param portType
     *            optional portType
     * @param createInstance
     *            is this a start activity
     * @param correlations
     *            the correlations used
     * @return
     */
    protected OPickReceive.OnMessage compileOnMessage(String varname, String plink, String operation,
            String messageExchangeId, QName portType, boolean createInstance, Collection<Correlation> correlations, String route) {

        OPickReceive.OnMessage onMessage = new OPickReceive.OnMessage(_context.getOProcess());
        onMessage.partnerLink = _context.resolvePartnerLink(plink);
        onMessage.operation = _context.resolveMyRoleOperation(onMessage.partnerLink, operation);
        if (onMessage.operation.getInput() != null && onMessage.operation.getInput().getMessage() != null)
            onMessage.variable = _context.resolveMessageVariable(varname, onMessage.operation.getInput().getMessage()
                    .getQName());
        onMessage.messageExchangeId = messageExchangeId;
        onMessage.route = route;

        if (portType != null && !portType.equals(onMessage.partnerLink.myRolePortType.getQName()))
            throw new CompilationException(__cmsgsGeneral.errPortTypeMismatch(portType,
                    onMessage.partnerLink.myRolePortType.getQName()));

        if (createInstance)
            onMessage.partnerLink.addCreateInstanceOperation(onMessage.operation);

        Set<String> csetNames = new HashSet<String>(); // prevents duplicate cset in on one set of correlations
        for (Correlation correlation : correlations) {
        	if( csetNames.contains(correlation.getCorrelationSet() ) ) {
                throw new CompilationException(__cmsgsGeneral.errDuplicateUseCorrelationSet(correlation
                        .getCorrelationSet()));
        	}
        	
            OScope.CorrelationSet cset = _context.resolveCorrelationSet(correlation.getCorrelationSet());

            switch (correlation.getInitiate()) {
            case UNSET:
            case NO:
                if (createInstance)
                    throw new CompilationException(__cmsgsGeneral.errUseOfUninitializedCorrelationSet(correlation
                            .getCorrelationSet()));
                onMessage.matchCorrelations.add(cset);
                onMessage.partnerLink.addCorrelationSetForOperation(onMessage.operation, cset, false);
                break;
            case YES:
                onMessage.initCorrelations.add(cset);
                break;
            case JOIN:
            	cset.hasJoinUseCases = true;
            	onMessage.joinCorrelations.add(cset);
                onMessage.partnerLink.addCorrelationSetForOperation(onMessage.operation, cset, true);
                break;

            default:
                    throw new AssertionError("Unexpected value for correlation set enumeration!");
            }

            for (OProcess.OProperty property : cset.properties) {
                // Force resolution of alias, to make sure that we have one for
                // this variable-property pair.
                _context.resolvePropertyAlias(onMessage.variable, property.name);
            }

            csetNames.add(correlation.getCorrelationSet());
        }

        if (!onMessage.partnerLink.hasMyRole()) {
            throw new CompilationException(__cmsgsGeneral.errNoMyRoleOnReceivePartnerLink(onMessage.partnerLink
                    .getName()));
        }

        return onMessage;
    }

}
