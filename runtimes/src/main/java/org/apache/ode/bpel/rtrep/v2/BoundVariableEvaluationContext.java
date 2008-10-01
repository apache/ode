package org.apache.ode.bpel.rtrep.v2;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.rapi.InvalidProcessException;
import org.apache.ode.utils.DOMUtils;

import javax.xml.namespace.QName;
import java.net.URI;
import java.util.HashMap;

/**
 * Expression language evaluation context used for expressions referencing a predefined variable
 * bound to a given value. Used for correlation matching based on a message value extraction
 * function (i.e. SimPEL+JS).
 */
public class BoundVariableEvaluationContext implements EvaluationContext {
    private HashMap<String, Element> _boundVars = new HashMap<String, Element>();

    public void addBoundVariable(String name, Element value) {
        _boundVars.put(name, value);
    }

    public Node readVariable(OScope.Variable variable, OMessageVarType.Part partDef) throws FaultException {
        // We need to tweak the context node based on what kind of variable (element vs non-element)
        Element data = _boundVars.get(variable.name);
        if (data == null) return null;

        if (partDef != null) {
            Element part = DOMUtils.findChildByName(data, new QName(null, partDef.name),false);
            if (part != null && partDef.type instanceof OElementVarType) {
                data = DOMUtils.findChildByName(part, ((OElementVarType)partDef.type).elementType);
            } else
                data = part;
        }
        return data;
    }

    public Node getPartData(Element message, OMessageVarType.Part part) throws FaultException {
        throw new InvalidProcessException("Part data not available in this context.");
    }

    public String readMessageProperty(OScope.Variable variable, OProcess.OProperty property) throws FaultException {
        throw new InvalidProcessException("Message property not available in this context.");
    }

    public boolean isLinkActive(OLink olink) throws FaultException {
        throw new InvalidProcessException("Links not available in this context.");
    }

    public Node getRootNode() {
        throw new InvalidProcessException("Root node not available in this context.");
    }

    public Node evaluateQuery(Node root, OExpression expr) throws FaultException {
        throw new InvalidProcessException("Root node querying not available in this context.");
    }

    public Long getProcessId() {
        throw new InvalidProcessException("Process id not available in this context.");
    }

    public boolean narrowTypes() {
        throw new InvalidProcessException("Types narrowing not available in this context.");
    }

    public URI getBaseResourceURI() {
        throw new InvalidProcessException("Base URI not available in this context.");
    }
}
