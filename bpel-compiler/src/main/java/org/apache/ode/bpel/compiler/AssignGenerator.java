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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.AssignActivity;
import org.apache.ode.bpel.compiler.bom.Copy;
import org.apache.ode.bpel.compiler.bom.ExtensionVal;
import org.apache.ode.bpel.compiler.bom.From;
import org.apache.ode.bpel.compiler.bom.LiteralVal;
import org.apache.ode.bpel.compiler.bom.PartnerLinkVal;
import org.apache.ode.bpel.compiler.bom.PropertyVal;
import org.apache.ode.bpel.compiler.bom.To;
import org.apache.ode.bpel.compiler.bom.VariableVal;
import org.apache.ode.bpel.o.DebugInfo;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OAssign;
import org.apache.ode.bpel.o.OVarType;
import org.apache.ode.bpel.o.OAssign.RValue;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Generates code for <code>&lt;assign&gt;</code> activities. 
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
class AssignGenerator extends DefaultActivityGenerator {
    private static final Log __log = LogFactory.getLog(AssignGenerator.class);

    private static final AssignGeneratorMessages __cmsgs =
        MessageBundle.getMessages(AssignGeneratorMessages.class);

    public OActivity newInstance(Activity src) {
        return new OAssign(_context.getOProcess(), _context.getCurrent());
    }

    public void compile(OActivity dest, Activity source) {
        OAssign oassign = (OAssign) dest;
        AssignActivity ad = (AssignActivity) source;
        for (Copy scopy : ad.getCopies()) {
            OAssign.Copy ocopy = new OAssign.Copy(_context.getOProcess());
            ocopy.keepSrcElementName = scopy.isKeepSrcElement();
            ocopy.ignoreMissingFromData = scopy.isIgnoreMissingFromData();
            ocopy.ignoreUninitializedFromVariable = scopy.isIgnoreUninitializedFromVariable();
            ocopy.insertMissingToData = scopy.isInsertMissingToData();
            ocopy.insertMissingToData = scopy.isInsertMissingToData();
            ocopy.debugInfo = new DebugInfo(_context.getSourceLocation(), scopy.getLineNo(),
                    source.getExtensibilityElements());
            try {
                if (scopy.getTo() == null)
                    throw new CompilationException(__cmsgs.errMissingToSpec().setSource(scopy));
                Object[] toResultType = new Object[1];
                ocopy.to = compileTo(scopy.getTo(), toResultType);

                if (scopy.getFrom() == null)
                    throw new CompilationException(__cmsgs.errMissingFromSpec().setSource(scopy));
                ocopy.from = compileFrom(scopy.getFrom(), toResultType[0]);
                
                verifyCopy(ocopy);
                oassign.copy.add(ocopy);

            } catch (CompilationException ce) {
                _context.recoveredFromError(scopy, ce);
            }
        }
    }

    /**
     * Verify that a copy follows the correct form.
     * 
     * @param ocopy
     */
    private void verifyCopy(OAssign.Copy ocopy) {
        if (__log.isDebugEnabled())
            __log.debug("verifying copy: " + ocopy);

        // If direct Message->Message copy
        if (ocopy.to instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.to).isMessageRef()
                && ocopy.from instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.from).isMessageRef()) {
            // Check that the LValue/RValue message types match up.
            String lvar = ((OAssign.VariableRef) ocopy.to).variable.name;
            String rvar = ((OAssign.VariableRef) ocopy.from).variable.name;
            QName tlvalue = ((OMessageVarType) ((OAssign.VariableRef) ocopy.to).variable.type).messageType;
            QName trvalue = ((OMessageVarType) ((OAssign.VariableRef) ocopy.from).variable.type).messageType;

            if (!tlvalue.equals(trvalue))
                throw new CompilationException(__cmsgs.errMismatchedMessageAssignment(lvar, tlvalue, rvar, trvalue));

        }

        // If Message->Non-Message copy
        else if (ocopy.from instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.from).isMessageRef()
                && (!(ocopy.to instanceof OAssign.VariableRef) || !((OAssign.VariableRef) ocopy.to).isMessageRef())) {
            String rval = ((OAssign.VariableRef) ocopy.from).variable.name;
            throw new CompilationException(__cmsgs.errCopyFromMessageToNonMessage(rval));

        }

        // If Non-Message->Message copy
        else if (ocopy.to instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.to).isMessageRef()
                && (!(ocopy.from instanceof OAssign.VariableRef) || !((OAssign.VariableRef) ocopy.from).isMessageRef())) {

            String lval = ((OAssign.VariableRef) ocopy.to).variable.name;
            throw new CompilationException(__cmsgs.errCopyToMessageFromNonMessage(lval));
        }

        // If *->Partner Link copy
        else if (ocopy.to instanceof OAssign.PartnerLinkRef
                && !((OAssign.PartnerLinkRef) ocopy.to).partnerLink.hasPartnerRole()) {
            String lval = ((OAssign.PartnerLinkRef) ocopy.to).partnerLink.getName();
            throw new CompilationException(__cmsgs.errCopyToUndeclaredPartnerRole(lval));
        }

        // If Partner Link->* copy
        else if (ocopy.from instanceof OAssign.PartnerLinkRef) {
            if (((OAssign.PartnerLinkRef) ocopy.from).isMyEndpointReference
                    && !((OAssign.PartnerLinkRef) ocopy.from).partnerLink.hasMyRole()) {
                String lval = ((OAssign.PartnerLinkRef) ocopy.from).partnerLink.getName();
                throw new CompilationException(__cmsgs.errCopyFromUndeclaredPartnerRole(lval, "myRole"));
            }
            if (!((OAssign.PartnerLinkRef) ocopy.from).isMyEndpointReference
                    && !((OAssign.PartnerLinkRef) ocopy.from).partnerLink.hasPartnerRole()) {
                String lval = ((OAssign.PartnerLinkRef) ocopy.from).partnerLink.getName();
                throw new CompilationException(__cmsgs.errCopyFromUndeclaredPartnerRole(lval, "partnerRole"));
            }
        }

        __log.debug("Copy verified OK: " + ocopy);
    }

    private OAssign.RValue compileFrom(From from, Object requestedResultType) {
        assert from != null;
        try {
            if (from.isExtensionVal()) {
                return compileExtensionVal(from.getAsExtensionVal());
            } else if (from.isLiteralVal()) {
                return compileLiteral(from.getAsLiteralVal());
            } else if (from.isPropertyVal()) {
                OAssign.PropertyRef pref = new OAssign.PropertyRef(_context.getOProcess());
                PropertyVal pval = from.getAsPropertyVal();
                pref.variable = _context.resolveVariable(pval.getVariable());
                pref.propertyAlias = _context.resolvePropertyAlias(pref.variable, pval.getProperty());
                return pref;
            } else if (from.isVariableVal()) {
                VariableVal vv = from.getAsVariableVal();
                OAssign.VariableRef vref = new OAssign.VariableRef(_context.getOProcess());
                vref.variable = _context.resolveVariable(vv.getVariable());
                OVarType rootNodeType = vref.variable.type;
                if (vv.getPart() != null) {
                    vref.part = _context.resolvePart(vref.variable, vv.getPart());
                    rootNodeType = vref.part.type;
                }
                if (vv.getHeader() != null) {
                    vref.headerPart = _context.resolveHeaderPart(vref.variable, vv.getHeader());
                    if (vref.headerPart == null)
                        vref.headerPart = new OMessageVarType.Part(_context.getOProcess(), vv.getHeader(), null);
                    rootNodeType = vref.headerPart.type;
                }
                if (vv.getLocation() != null && vv.getLocation().getExpression() != null)
                    vref.location = _context.compileExpr(vv.getLocation(), rootNodeType, requestedResultType, new Object[1]);
                return vref;
            } else if (from.isPartnerLinkVal()) {
                PartnerLinkVal plv = from.getAsPartnerLinkVal();
                OAssign.PartnerLinkRef plref = new OAssign.PartnerLinkRef(_context.getOProcess());
                plref.partnerLink = _context.resolvePartnerLink(plv.getPartnerLink());
                plref.isMyEndpointReference = (plv.getEndpointReference() == PartnerLinkVal.EndpointReference.MYROLE);
                return plref;
            } else if (from.getAsExpression() != null) {
                return new OAssign.Expression(_context.getOProcess(), _context.compileExpr(from.getAsExpression(), null, requestedResultType, new Object[1]));
            }

            throw new CompilationException(__cmsgs.errUnkownFromSpec().setSource(from));

        } catch (CompilationException ce) {
            if (ce.getCompilationMessage().source == null)
                ce.getCompilationMessage().source = from;
            throw ce;
        }
    }

    /**
     * Compile an extension to/from-spec. Extension to/from-specs are compiled into 
     * "DirectRef"s. 
     * 
     * @param extVal source representation
     * @return compiled representation
     */
    private RValue compileExtensionVal(ExtensionVal extVal) {
        OAssign.DirectRef dref = new OAssign.DirectRef(_context.getOProcess());
        dref.variable = _context.resolveVariable(extVal.getVariable());
        dref.elName =  extVal.getExtension();
        return dref;
    }

    private OAssign.RValue compileLiteral(LiteralVal from) {
        Element literal = from.getLiteral();
        Document newDoc = DOMUtils.newDocument();
        Element clone = (Element) newDoc.importNode(literal, true);
        newDoc.appendChild(clone);
        return new OAssign.Literal(_context.getOProcess(), newDoc);
    }

    private OAssign.LValue compileTo(To to, Object[] resultType) {
        assert to != null;

        try {
            if (to.isPropertyVal()) {
                OAssign.PropertyRef pref = new OAssign.PropertyRef(_context.getOProcess());
                pref.variable = _context.resolveVariable(to.getAsPropertyVal().getVariable());
                pref.propertyAlias = _context.resolvePropertyAlias(pref.variable, to.getAsPropertyVal().getProperty());
                return pref;
            } else if (to.isVariableVal()) {
                VariableVal vv = to.getAsVariableVal();
                OAssign.VariableRef vref = new OAssign.VariableRef(_context.getOProcess());
                vref.variable = _context.resolveVariable(vv.getVariable());
                OVarType rootNodeType = vref.variable.type;
                if (to.getAsVariableVal().getPart() != null) {
                    vref.part = _context.resolvePart(vref.variable, vv.getPart());
                    rootNodeType = vref.part.type;
                }
                if (to.getAsVariableVal().getHeader() != null) {
                    vref.headerPart = _context.resolveHeaderPart(vref.variable, vv.getHeader());
                    if (vref.headerPart == null)
                        vref.headerPart = new OMessageVarType.Part(_context.getOProcess(), to.getAsVariableVal().getHeader(), null);
                    rootNodeType = vref.headerPart.type;
                }
                resultType[0] = rootNodeType;
                if (vv.getLocation() != null && vv.getLocation().getExpression() != null)
                    vref.location = _context.compileExpr(vv.getLocation(), rootNodeType, null, resultType);
                return vref;
            } else if (to.isPartnerLinkVal()) {
                OAssign.PartnerLinkRef plref = new OAssign.PartnerLinkRef(_context.getOProcess());
                plref.partnerLink = _context.resolvePartnerLink(to.getAsPartnerLinkVal().getPartnerLink());
                return plref;
            } else if (to.getAsExpression() != null){
                return new OAssign.LValueExpression(_context.getOProcess(), _context
                        .compileLValueExpr(to.getAsExpression(), null, null, resultType));
            }

            throw new CompilationException(__cmsgs.errUnknownToSpec().setSource(to));
        } catch (CompilationException ce) {
            if (ce.getCompilationMessage().source == null)
                ce.getCompilationMessage().source = to;
            throw ce;
        }
    }

}
