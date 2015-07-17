package org.apache.ode.bpel.elang.xpath10.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL20;
import org.apache.ode.bpel.elang.xpath10.obj.OXPath10Expression;
import org.apache.ode.bpel.elang.xpath20.runtime.MockCompilerContext;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.explang.EvaluationException;
import org.apache.ode.bpel.obj.OExpression;
import org.apache.ode.bpel.obj.OLink;
import org.apache.ode.bpel.obj.OMessageVarType.Part;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.bpel.obj.OScope.Variable;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XPath10ExpressionRuntimeTest implements EvaluationContext {

    private XPath10ExpressionRuntime _runtime;
    private XPath10ExpressionCompilerBPEL20 _compiler;

    private MockCompilerContext _cc;
    
    @Before
    public void setUp() throws Exception {
        _cc = new MockCompilerContext();
        _runtime = new XPath10ExpressionRuntime();
        _runtime.initialize(new HashMap());
        _compiler = new XPath10ExpressionCompilerBPEL20();
        _compiler.setCompilerContext(_cc);
    }
    
    @After
    public void tearDown() throws Exception {
        _cc = null;
        _runtime = null;
        _compiler = null;
    }

    private OXPath10Expression compile(String xpath) {
        Document doc = DOMUtils.newDocument();
        Element e = doc.createElementNS(null, "expression");
        doc.appendChild(e);
        e.appendChild(doc.createTextNode(xpath));
        Expression exp = new Expression(e);
        return (OXPath10Expression)_compiler.compileLValue(exp);
    }

    @Test
    public void testEvaluate_NaN() throws Exception {
        OXPath10Expression exp = compile("number('/tns:Title/tns:Data')");
        NSContext context = new NSContext();
        context.register("tns", "http://foobar");
        exp.setNamespaceCtx(context);

        Node retVal = _runtime.evaluateNode(exp, this);

        assertNotNull(retVal);
        assertEquals(String.valueOf(Double.NaN), retVal.getTextContent());
    }

    public Node readVariable(Variable variable, Part part)
            throws FaultException {
        return null;
    }

    public Node getPartData(Element message, Part part) throws FaultException {
        return null;
    }

    public String readMessageProperty(OScope.Variable variable, OProcess.OProperty property)
            throws FaultException {
        return null;
    }

    public boolean isLinkActive(OLink olink) throws FaultException {
        return false;
    }

    public Node getRootNode() {
        return null;
    }

    public Node evaluateQuery(Node root, OExpression expr)
            throws FaultException, EvaluationException {
        return null;
    }

    public Long getProcessId() {
        return null;
    }

    public QName getProcessQName() {
        return null;
    }

    public boolean narrowTypes() {
        return false;
    }

    public URI getBaseResourceURI() {
        return null;
    }

    public Node getPropertyValue(QName propertyName) {
        return null;
    }

    public Date getCurrentEventDateTime() {
        return null;
    }
}
