package org.apache.ode.bpel.elang.xpath20.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import org.apache.ode.bpel.compiler.bom.*;
import org.apache.ode.bpel.compiler.api.*;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLValueExpression;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OXsdTypeVarType;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.o.OProcess.OProperty;
import org.apache.ode.bpel.o.OProcess.OPropertyAlias;
import org.apache.ode.bpel.o.OScope.CorrelationSet;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.NSContext;

public class MockCompilerContext implements CompilerContext {
    private OProcess _oprocess = new OProcess("20");
    private Map<String , Variable> _vars =new  HashMap<String, Variable>();
    
    public OExpression constantExpr(boolean value) {
        // TODO Auto-generated method stub
        return null;
    }
    public OExpression compileJoinCondition(Expression expr) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OExpression compileExpr(Expression expr) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OLValueExpression compileLValueExpr(Expression expr) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OXslSheet compileXslt(String docStrUri) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OXsdTypeVarType resolveXsdType(QName typeName) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OProperty resolveProperty(QName name) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Variable resolveVariable(String name) throws CompilationException {
        return _vars.get(name);
    }

    public List<Variable> getAccessibleVariables() {
        return new ArrayList<Variable>(_vars.values());
    }

    public Variable resolveMessageVariable(String inputVar) throws CompilationException {
        return _vars.get(inputVar);
    }

    public Variable resolveMessageVariable(String inputVar, QName messageType) throws CompilationException {
        return _vars.get(inputVar);
    }

    public Part resolvePart(Variable variable, String partname) throws CompilationException {
        return ((OMessageVarType)variable.type).parts.get(partname);
    }

    public OActivity compile(Activity child) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OActivity compileSLC(Activity source) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OPartnerLink resolvePartnerLink(String name) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Operation resolvePartnerRoleOperation(OPartnerLink partnerLink, String operationName) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public Operation resolveMyRoleOperation(OPartnerLink partnerLink, String operationName) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OPropertyAlias resolvePropertyAlias(Variable variable, QName property) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public void recoveredFromError(Object where, CompilationException bce) throws CompilationException {
        // TODO Auto-generated method stub
        
    }

    public OLink resolveLink(String linkName) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OScope resolveCompensatableScope(String scopeToCompensate) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public OProcess getOProcess() throws CompilationException {
        return _oprocess;
    }

    public CorrelationSet resolveCorrelationSet(String csetName) throws CompilationException {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSourceLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    public void compile(OActivity context, BpelObject activity, Runnable run) {
        // TODO Auto-generated method stub
        
    }

    public boolean isPartnerLinkAssigned(String plink) {
        // TODO Auto-generated method stub
        return false;
    }

    public List<OActivity> getActivityStack() {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerElementVar(String name, QName type) {
        OElementVarType varType = new OElementVarType(getOProcess(),type);
        OScope.Variable var = new OScope.Variable(getOProcess(),varType);
        var.name = name;
        _vars.put(name, var);
    }
    public OExpression compileExpr(String locationstr, NSContext nsContext) {
        // TODO Auto-generated method stub
        return null;
    }
    public OActivity getCurrent() {
        // TODO Auto-generated method stub
        return null;
    }

}
