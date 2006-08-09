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
import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.AssignActivity;
import org.apache.ode.bom.api.Copy;
import org.apache.ode.bom.api.ExpressionVal;
import org.apache.ode.bom.api.From;
import org.apache.ode.bom.api.LiteralVal;
import org.apache.ode.bom.api.PartnerLinkVal;
import org.apache.ode.bom.api.PropertyVal;
import org.apache.ode.bom.api.To;
import org.apache.ode.bom.api.VariableVal;
import org.apache.ode.bom.impl.nodes.ExpressionValImpl;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.o.DebugInfo;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OAssign;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;


/**
 * Generates code for <code>&lt;assign&gt;</code> activities. 
 * This class simply extends {@link DefaultActivityGenerator} as the default
 * implentation suffices. 
 */
class AssignGenerator extends  DefaultActivityGenerator {
	private static final Log __log = LogFactory.getLog(AssignGenerator.class);

  private static final AssignGeneratorMessages __cmsgs =
    MessageBundle.getMessages(AssignGeneratorMessages.class);

  public OActivity newInstance(Activity src) {
    return new OAssign(_context.getOProcess());
  }

  public void compile(OActivity dest, Activity source) {
    OAssign oassign = (OAssign)dest;
    AssignActivity ad = (AssignActivity)source;
    for (Copy scopy : ad.getCopies()) {
      OAssign.Copy ocopy = new OAssign.Copy(_context.getOProcess());
      ocopy.keepSrcElementName = scopy.isKeepSrcElement();
      ocopy.debugInfo = new DebugInfo(_context.getSourceLocation(), scopy.getLineNo(),
              source.getExtensibilityElements());
      try {
        if (scopy.getFrom() == null)
          throw new CompilationException(__cmsgs.errMissingFromSpec().setSource(scopy));
        ocopy.from = compileFrom(scopy.getFrom());
        if (scopy.getTo() == null)
          throw new CompilationException(__cmsgs.errMissingToSpec().setSource(scopy));
        ocopy.to = compileTo(scopy.getTo());

        verifyCopy(ocopy);
        oassign.copy.add(ocopy);

      } catch (CompilationException ce) {
        _context.recoveredFromError(scopy, ce);
      }
    }
  }

  /**
   * Verify that a copy follows the correct form. 
   * @param ocopy
   */
  private void verifyCopy(OAssign.Copy ocopy) {
  	if (__log.isDebugEnabled()) 
  		__log.debug("verifying copy: " + ocopy);
  	
  	// If direct Message->Message copy
  	if (ocopy.to instanceof OAssign.VariableRef 
  			&& ((OAssign.VariableRef)ocopy.to).isMessageRef()
  			&& ocopy.from instanceof OAssign.VariableRef
  			&& ((OAssign.VariableRef)ocopy.from).isMessageRef()) {
  		// Check that the LValue/RValue message types 
  		// match up.
  		String lvar = ((OAssign.VariableRef)ocopy.to).variable.name;
  		String rvar = ((OAssign.VariableRef)ocopy.from).variable.name;
  		QName tlvalue = ((OMessageVarType)((OAssign.VariableRef)ocopy.to).variable.type).messageType;
  		QName trvalue = ((OMessageVarType)((OAssign.VariableRef)ocopy.from).variable.type).messageType;
  		
  		if (!tlvalue.equals(trvalue))
  			throw new CompilationException(__cmsgs.errMismatchedMessageAssignment(lvar, tlvalue,rvar,trvalue));
  	
  	} 
  
  	// If Message->Non-Message copy 
  	else if (ocopy.from instanceof OAssign.VariableRef 
  			&& ((OAssign.VariableRef)ocopy.from).isMessageRef()
  			&& ( !(ocopy.to instanceof OAssign.VariableRef) || 
  					 !((OAssign.VariableRef)ocopy.to).isMessageRef())) {
  		String rval = ((OAssign.VariableRef)ocopy.from).variable.name;
  		throw new CompilationException(__cmsgs.errCopyFromMessageToNonMessage(rval));
  			
  	}
  	
  	// If Non-Message->Message copy 
  	else if (ocopy.to instanceof OAssign.VariableRef 
  			&& ((OAssign.VariableRef)ocopy.to).isMessageRef()
  			&& ( !(ocopy.from instanceof OAssign.VariableRef) ||
  			     !((OAssign.VariableRef)ocopy.from).isMessageRef())) {
  		
  		String lval = ((OAssign.VariableRef)ocopy.to).variable.name;
  		throw new CompilationException(__cmsgs.errCopyToMessageFromNonMessage(lval));
  	}

    // If  *->Partner Link copy
    else if (ocopy.to instanceof OAssign.PartnerLinkRef
        && !((OAssign.PartnerLinkRef)ocopy.to).partnerLink.hasPartnerRole()) {
      String lval = ((OAssign.PartnerLinkRef)ocopy.to).partnerLink.getName();
      throw new CompilationException(__cmsgs.errCopyToUndeclaredPartnerRole(lval));
    }

    // If  Partner Link->* copy
    else if (ocopy.from instanceof OAssign.PartnerLinkRef) {
      if (((OAssign.PartnerLinkRef)ocopy.from).isMyEndpointReference
              && !((OAssign.PartnerLinkRef)ocopy.from).partnerLink.hasMyRole()) {
        String lval = ((OAssign.PartnerLinkRef)ocopy.from).partnerLink.getName();
        throw new CompilationException(__cmsgs.errCopyFromUndeclaredPartnerRole(lval, "myRole"));
      }
      if (!((OAssign.PartnerLinkRef)ocopy.from).isMyEndpointReference
              && !((OAssign.PartnerLinkRef)ocopy.from).partnerLink.hasPartnerRole()) {
        String lval = ((OAssign.PartnerLinkRef)ocopy.from).partnerLink.getName();
        throw new CompilationException(__cmsgs.errCopyFromUndeclaredPartnerRole(lval, "partnerRole"));
      }
    }

    __log.debug("Copy verified OK: " + ocopy);
  }
  
	private OAssign.RValue compileFrom(From from) {
    assert from != null;
    try {
      if (from instanceof LiteralVal) {
        return compileLiteral((LiteralVal) from);
      } else if (from instanceof ExpressionVal) {
        return new OAssign.Expression(_context.getOProcess(), _context.compileExpr(((ExpressionValImpl)from).getExpression()));
      } else if (from instanceof PropertyVal) {
        OAssign.PropertyRef pref =  new OAssign.PropertyRef(_context.getOProcess());
        pref.variable = _context.resolveVariable(((PropertyVal)from).getVariable());
        pref.propertyAlias = _context.resolvePropertyAlias(pref.variable,((PropertyVal)from).getProperty());
        return pref;
      } else if (from instanceof VariableVal) {
        OAssign.VariableRef vref = new OAssign.VariableRef(_context.getOProcess());
        vref.variable = _context.resolveVariable(((VariableVal)from).getVariable());
        if (((VariableVal)from).getPart() != null) {
          vref.part = _context.resolvePart(vref.variable, ((VariableVal)from).getPart());
          if (((VariableVal)from).getLocation() != null)
            vref.location = _context.compileExpr(((VariableVal)from).getLocation());
        }
        // TODO: check for irrelevant properties.
        return vref;
      } else if (from instanceof PartnerLinkVal) {
        OAssign.PartnerLinkRef plref = new OAssign.PartnerLinkRef(_context.getOProcess());
        plref.partnerLink = _context.resolvePartnerLink(((PartnerLinkVal)from).getPartnerLink());
        plref.isMyEndpointReference = "myRole".equals(((PartnerLinkVal)from).getEndpointReference());
        return plref;
      }

      throw new CompilationException(__cmsgs.errUnkownFromSpec().setSource(from));

    } catch (CompilationException ce) {
      if (ce.getCompilationMessage().source == null)
        ce.getCompilationMessage().source = from;
      throw ce;
    }
  }

  private OAssign.RValue compileLiteral(LiteralVal from) {
    Element literal = from.getLiteral();
    Document newDoc = DOMUtils.newDocument();
    Element clone = (Element) newDoc.importNode(literal, true);
    newDoc.appendChild(clone);
    return new OAssign.Literal(_context.getOProcess(), newDoc);
  }

  private OAssign.LValue compileTo(To to) {
    assert to != null;

    try {
      if (to instanceof PropertyVal) {
        OAssign.PropertyRef pref =  new OAssign.PropertyRef(_context.getOProcess());
        pref.variable = _context.resolveVariable(((PropertyVal)to).getVariable());
        pref.propertyAlias = _context.resolvePropertyAlias(pref.variable,((PropertyVal)to).getProperty());
        return pref;
      } else if (to instanceof VariableVal) {
        OAssign.VariableRef vref = new OAssign.VariableRef(_context.getOProcess());
        vref.variable = _context.resolveVariable(((VariableVal)to).getVariable());
        if (((VariableVal)to).getPart() != null) {
          vref.part = _context.resolvePart(vref.variable, ((VariableVal)to).getPart());
          if (((VariableVal)to).getLocation() != null)
            vref.location = _context.compileExpr(((VariableVal)to).getLocation());
        }
//      TODO: check for irrelevant properties.
        return vref;
      } else if (to instanceof ExpressionVal) {
        return new OAssign.LValueExpression(_context.getOProcess(), _context.compileLValueExpr(((ExpressionValImpl)to).getExpression()));
      } else if (to instanceof PartnerLinkVal) {
        OAssign.PartnerLinkRef plref = new OAssign.PartnerLinkRef(_context.getOProcess());
        plref.partnerLink = _context.resolvePartnerLink(((PartnerLinkVal)to).getPartnerLink());
        return plref;
      }

      throw new CompilationException(__cmsgs.errUnknownToSpec().setSource(to));
    } catch (CompilationException ce) {
      if (ce.getCompilationMessage().source == null)
        ce.getCompilationMessage().source = to;
      throw ce;
    }
  }

}
