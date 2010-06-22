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
package org.apache.ode.bpel.compiler.bom;

import java.util.Collections;
import java.util.List;


import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.w3c.dom.Element;

/**
 * Base-class for BPEL scope-like constructs. The term "scope-like construct"
 * encompasses those structures that have the potential to declare either
 * variables, event handlers, fault handlers, or compensation handlers. These
 * scope like constructs include the scope activity, event handlers, the invoke
 * activity, etc...
 */
public class Scope extends JoinFailureSuppressor {

    public Scope(Element el) {
        super(el);
    }

    /**
     * Get the compensation handler for this scope.
     *
     * @return the compensation handler
     */
    public CompensationHandler getCompensationHandler() {
        return getFirstChild(CompensationHandler.class);
    }

    /**
     * Gets the termination handler for this scope.
     *
     * @return terminationHandler
     */
    public TerminationHandler getTerminationHandler() {
        return getFirstChild(TerminationHandler.class);
    }

    /**
     * Get the fault handler for this scope.
     *
     * @return the fault handler
     */
    public FaultHandler getFaultHandler() {
        return getFirstChild(FaultHandler.class);
    }

    /**
     * Get correlation sets for this scope.
     *
     * @return correlation sets for this scope.
     */
    public List<CorrelationSet> getCorrelationSetDecls() {
        CorrelationSets csets = getFirstChild(CorrelationSets.class);
        if (csets == null)
            return Collections.emptyList();
        return csets.getChildren(CorrelationSet.class);
    }

    /**
     * Get a correlation set decleration.
     *
     * @param setName
     *            name of correlation set
     * @return {@link CorrelationSet} of the fiven name.
     */
    public CorrelationSet getCorrelationSetDecl(final String setName) {
        return CollectionsX.find_if(getCorrelationSetDecls(), new MemberOfFunction<CorrelationSet>() {

            @Override
            public boolean isMember(CorrelationSet cs) {
                return setName.equals(cs.getName());
            }

        });

    }

    /**
     * DOCUMENTME
     *
     * @param varName
     *            DOCUMENTME
     * @return DOCUMENTME
     */
    public Variable getVariableDecl(final String varName) {
        return CollectionsX.find_if(getVariables(), new MemberOfFunction<Variable>() {

            @Override
            public boolean isMember(Variable v) {
                return varName.equals(v.getName());
            }

        });
    }

    /**
     */
    public List<Variable> getVariables() {
        BpelObject vars = getFirstChild(Variables.class);
        if (vars == null)
            return Collections.emptyList();
        return vars.getChildren(Variable.class);
    }

    /**
     * Get an array of <code>OnAlarmEventHandler</code>s for this scope.
     *
     * @return the <code>OnAlarmEventHandler</code>s
     */
    public List<OnAlarm> getAlarms() {
        BpelObject eventHandlers = getFirstChild(rewriteTargetNS(Bpel20QNames.EVENTHANDLERS));
        if (eventHandlers == null)
            return Collections.emptyList();
        return eventHandlers.getChildren(OnAlarm.class);
    }

    /**
     */
    public List<OnEvent> getEvents() {
        BpelObject eventHandlers = getFirstChild(rewriteTargetNS(Bpel20QNames.EVENTHANDLERS));
        if (eventHandlers == null)
            return Collections.emptyList();

        return eventHandlers.getChildren(OnEvent.class);
    }

    /**
     */
    public boolean isVariableAccessSerialized() {
        return false;
    }


    public Boolean getAtomicScope() {
        String value = getAttribute("atomic", null);
        if ("yes".equals(value))
            return Boolean.TRUE;
        if ("no".equals(value))
            return Boolean.FALSE;
        return Boolean.FALSE;
    }
    
    public Boolean getIsolatedScope() {
        String value = getAttribute("isolated", "no");
        if ("yes".equals(value))
            return Boolean.TRUE;
        if ("no".equals(value))
            return Boolean.FALSE;
        return null;
    }
    /**
     * Get a partnerLink declared in this scope.
     *
     * @param partnerLinkName
     *            name of partner link
     */
    public PartnerLink getPartnerLink(final String partnerLinkName) {
        return CollectionsX.find_if(getPartnerLinks(), new MemberOfFunction<PartnerLink>() {

            @Override
            public boolean isMember(PartnerLink pl) {
                return partnerLinkName.equals(pl.getName());
            }

        });

    }

    /**
     * Get all partnerLinks delcared in this scope.
     *
     * @return set of declared {@link PartnerLink}s.
     */
    public List<PartnerLink> getPartnerLinks() {
        PartnerLinks plinks = getFirstChild(PartnerLinks.class);
        if (plinks == null)
            return Collections.emptyList();
        return plinks.getChildren(PartnerLink.class);
    }

}
