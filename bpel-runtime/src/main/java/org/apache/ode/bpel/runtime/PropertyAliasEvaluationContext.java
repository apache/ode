/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.o.*;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * Expression language evaluation context used for evaluating property aliases.
 */
public class PropertyAliasEvaluationContext implements EvaluationContext {
  private Element _root;

  public PropertyAliasEvaluationContext(Element msgData, OProcess.OPropertyAlias alias) {
	  // We need to tweak the context node based on what kind of variable (element vs non-element)
	  if (alias.part == null) {
		 // actually, this should not happen
		 
    	_root  = msgData;
	  } else {
    	Element part = DOMUtils.findChildByName(msgData,new QName(null, alias.part.name),false);
      if (part != null && alias.part.type instanceof OElementVarType) {
    		_root = DOMUtils.findChildByName(part, ((OElementVarType)alias.part.type).elementType);
      } else
    		_root = part;
	  }
    
  }

  public Node getRootNode() {
    return _root;
  }

  public boolean isLinkActive(OLink olink) throws FaultException {
    throw new InvalidProcessException("Link status not available in this context.");
  }

  public String readMessageProperty(OScope.Variable variable, OProcess.OProperty property) throws FaultException {
    throw new InvalidProcessException("Message properties not available in this context.");
  }


  public Node readVariable(OScope.Variable variable, OMessageVarType.Part part) throws FaultException {
    throw new InvalidProcessException("Message variables not available in this context.");
  }

  public Node evaluateQuery(Node root, OExpression expr) throws FaultException {
	    throw new InvalidProcessException("Query language not available in this context.");
  }

  public Long getProcessId() {
    throw new InvalidProcessException("Process execution information not available in this context.");
  }

  public Node getPartData(Element message, Part part) throws FaultException {
 	// TODO Auto-generated method stub
	return null;
  }

}
