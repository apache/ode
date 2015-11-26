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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.apache.ode.bpel.obj.DebugInfo;
import org.apache.ode.bpel.obj.OActivity;
import org.apache.ode.bpel.obj.OAssign;
import org.apache.ode.bpel.obj.OVarType;
import org.apache.ode.bpel.obj.OAssign.RValue;
import org.apache.ode.bpel.obj.OMessageVarType;
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
    private static final Logger __log = LoggerFactory.getLogger(AssignGenerator.class);

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
            ocopy.setKeepSrcElementName(scopy.isKeepSrcElement());
            ocopy.setIgnoreMissingFromData(scopy.isIgnoreMissingFromData());
            ocopy.setIgnoreUninitializedFromVariable(scopy.isIgnoreUninitializedFromVariable());
            ocopy.setInsertMissingToData(scopy.isInsertMissingToData());
            ocopy.setInsertMissingToData(scopy.isInsertMissingToData());
            ocopy.setDebugInfo(new DebugInfo(_context.getSourceLocation() , scopy.getLineNo() , source.getExtensibilityElements()));
            try {
                if (scopy.getTo() == null)
                    throw new CompilationException(__cmsgs.errMissingToSpec().setSource(scopy));
                Object[] toResultType = new Object[1];
                ocopy.setTo(compileTo(scopy.getTo(), toResultType));

                if (scopy.getFrom() == null)
                    throw new CompilationException(__cmsgs.errMissingFromSpec().setSource(scopy));
                ocopy.setFrom(compileFrom(scopy.getFrom(), toResultType[0]));

                verifyCopy(ocopy);
                oassign.getCopy().add(ocopy);

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
        if (ocopy.getTo() instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.getTo()).isMessageRef()
                && ocopy.getFrom() instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.getFrom()).isMessageRef()) {
            // Check that the LValue/RValue message types match up.
            String lvar = ((OAssign.VariableRef) ocopy.getTo()).getVariable().getName();
            String rvar = ((OAssign.VariableRef) ocopy.getFrom()).getVariable().getName();
            QName tlvalue = ((OMessageVarType) ((OAssign.VariableRef) ocopy.getTo()).getVariable().getType()).getMessageType();
            QName trvalue = ((OMessageVarType) ((OAssign.VariableRef) ocopy.getFrom()).getVariable().getType()).getMessageType();

            if (!tlvalue.equals(trvalue))
                throw new CompilationException(__cmsgs.errMismatchedMessageAssignment(lvar, tlvalue, rvar, trvalue));

        }

        // If Message->Non-Message copy
        else if (ocopy.getFrom() instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.getFrom()).isMessageRef()
                && (!(ocopy.getTo() instanceof OAssign.VariableRef) || !((OAssign.VariableRef) ocopy.getTo()).isMessageRef())) {
            String rval = ((OAssign.VariableRef) ocopy.getFrom()).getVariable().getName();
            throw new CompilationException(__cmsgs.errCopyFromMessageToNonMessage(rval));

        }

        // If Non-Message->Message copy
        else if (ocopy.getTo() instanceof OAssign.VariableRef && ((OAssign.VariableRef) ocopy.getTo()).isMessageRef()
                && (!(ocopy.getFrom() instanceof OAssign.VariableRef) || !((OAssign.VariableRef) ocopy.getFrom()).isMessageRef())) {

            String lval = ((OAssign.VariableRef) ocopy.getTo()).getVariable().getName();
            throw new CompilationException(__cmsgs.errCopyToMessageFromNonMessage(lval));
        }

        // If *->Partner Link copy
        else if (ocopy.getTo() instanceof OAssign.PartnerLinkRef
                && !((OAssign.PartnerLinkRef) ocopy.getTo()).getPartnerLink().hasPartnerRole()) {
            String lval = ((OAssign.PartnerLinkRef) ocopy.getTo()).getPartnerLink().getName();
            throw new CompilationException(__cmsgs.errCopyToUndeclaredPartnerRole(lval));
        }

        // If Partner Link->* copy
        else if (ocopy.getFrom() instanceof OAssign.PartnerLinkRef) {
            if (((OAssign.PartnerLinkRef) ocopy.getFrom()).isIsMyEndpointReference()
                    && !((OAssign.PartnerLinkRef) ocopy.getFrom()).getPartnerLink().hasMyRole()) {
                String lval = ((OAssign.PartnerLinkRef) ocopy.getFrom()).getPartnerLink().getName();
                throw new CompilationException(__cmsgs.errCopyFromUndeclaredPartnerRole(lval, "myRole"));
            }
            if (!((OAssign.PartnerLinkRef) ocopy.getFrom()).isIsMyEndpointReference()
                    && !((OAssign.PartnerLinkRef) ocopy.getFrom()).getPartnerLink().hasPartnerRole()) {
                String lval = ((OAssign.PartnerLinkRef) ocopy.getFrom()).getPartnerLink().getName();
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
                pref.setVariable(_context.resolveVariable(pval.getVariable()));
                pref.setPropertyAlias(_context.resolvePropertyAlias(pref.getVariable(), pval.getProperty()));
                return pref;
            } else if (from.isVariableVal()) {
                VariableVal vv = from.getAsVariableVal();
                OAssign.VariableRef vref = new OAssign.VariableRef(_context.getOProcess());
                vref.setVariable(_context.resolveVariable(vv.getVariable()));
                OVarType rootNodeType = vref.getVariable().getType();
                if (vv.getPart() != null) {
                    vref.setPart(_context.resolvePart(vref.getVariable(), vv.getPart()));
                    rootNodeType = vref.getPart().getType();
                }
                if (vv.getHeader() != null) {
                    vref.setHeaderPart(_context.resolveHeaderPart(vref.getVariable(), vv.getHeader()));
                    if (vref.getHeaderPart() == null)
                        vref.setHeaderPart(new org.apache.ode.bpel.obj.OMessageVarType.Part(_context.getOProcess() , vv.getHeader() , null));
                    rootNodeType = vref.getHeaderPart().getType();
                }
                if (vv.getLocation() != null && vv.getLocation().getExpression() != null)
                    vref.setLocation(_context.compileExpr(vv.getLocation(), rootNodeType, requestedResultType, new java.lang.Object[1]));
                return vref;
            } else if (from.isPartnerLinkVal()) {
                PartnerLinkVal plv = from.getAsPartnerLinkVal();
                OAssign.PartnerLinkRef plref = new OAssign.PartnerLinkRef(_context.getOProcess());
                plref.setPartnerLink(_context.resolvePartnerLink(plv.getPartnerLink()));
                plref.setIsMyEndpointReference((plv.getEndpointReference()) == (org.apache.ode.bpel.compiler.bom.PartnerLinkVal.EndpointReference.MYROLE));
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
        dref.setVariable(_context.resolveVariable(extVal.getVariable()));
        dref.setElName(extVal.getExtension());
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
                pref.setVariable(_context.resolveVariable(to.getAsPropertyVal().getVariable()));
                pref.setPropertyAlias(_context.resolvePropertyAlias(pref.getVariable(), to.getAsPropertyVal().getProperty()));
                return pref;
            } else if (to.isVariableVal()) {
                VariableVal vv = to.getAsVariableVal();
                OAssign.VariableRef vref = new OAssign.VariableRef(_context.getOProcess());
                vref.setVariable(_context.resolveVariable(vv.getVariable()));
                OVarType rootNodeType = vref.getVariable().getType();
                if (to.getAsVariableVal().getPart() != null) {
                    vref.setPart(_context.resolvePart(vref.getVariable(), vv.getPart()));
                    rootNodeType = vref.getPart().getType();
                }
                if (to.getAsVariableVal().getHeader() != null) {
                    vref.setHeaderPart(_context.resolveHeaderPart(vref.getVariable(), vv.getHeader()));
                    if (vref.getHeaderPart() == null)
                        vref.setHeaderPart(new org.apache.ode.bpel.obj.OMessageVarType.Part(_context.getOProcess() , to.getAsVariableVal().getHeader() , null));
                    rootNodeType = vref.getHeaderPart().getType();
                }
                resultType[0] = rootNodeType;
                if (vv.getLocation() != null && vv.getLocation().getExpression() != null)
                    vref.setLocation(_context.compileExpr(vv.getLocation(), rootNodeType, null, resultType));
                return vref;
            } else if (to.isPartnerLinkVal()) {
                OAssign.PartnerLinkRef plref = new OAssign.PartnerLinkRef(_context.getOProcess());
                plref.setPartnerLink(_context.resolvePartnerLink(to.getAsPartnerLinkVal().getPartnerLink()));
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
