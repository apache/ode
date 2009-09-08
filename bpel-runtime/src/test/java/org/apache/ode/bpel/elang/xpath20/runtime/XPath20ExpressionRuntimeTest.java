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
package org.apache.ode.bpel.elang.xpath20.runtime;

import junit.framework.TestCase;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.elang.xpath20.compiler.XPath20ExpressionCompilerBPEL20;
import org.apache.ode.bpel.elang.xpath20.o.OXPath20ExpressionBPEL20;
import org.apache.ode.bpel.explang.EvaluationContext;
import org.apache.ode.bpel.o.OExpression;
import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.o.OProcess.OProperty;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class XPath20ExpressionRuntimeTest extends TestCase implements EvaluationContext {
    
    private XPath20ExpressionRuntime _runtime;
    private Map<String, Node> _vars;
    private XPath20ExpressionCompilerBPEL20 _compiler;

    private MockCompilerContext _cc;
    private Document _vardoc;
    public XPath20ExpressionRuntimeTest() {}
    
    @Override
    public void setUp() throws Exception {
        _vars = new HashMap<String, Node>();
        _cc = new MockCompilerContext();
        _runtime = new XPath20ExpressionRuntime();       
        _runtime.initialize(new HashMap());
        _compiler = new XPath20ExpressionCompilerBPEL20();
        _compiler.setCompilerContext(_cc);
        
        _vardoc = DOMUtils.parse(getClass().getResourceAsStream("/xpath20/variables.xml"));
        NodeList variables = _vardoc.getDocumentElement().getChildNodes();
        for (int i = 0; i < variables.getLength(); ++i) {
            Node n = variables.item(i);
            if (n.getNodeType()!=Node.ELEMENT_NODE)
                continue;
            Element v = (Element) n;
            v.normalize();
            if (v.getLocalName().equals("elementVar")) {
                String name = v.getAttribute("name");
                Node cn = v.getFirstChild();
                while (cn != null && cn.getNodeType() != Node.ELEMENT_NODE)
                    cn = cn.getNextSibling();
                Element el = (Element)cn;
                _cc.registerElementVar(name, new QName(el.getNamespaceURI(),el.getLocalName()));
                _vars.put(name,el);
            }
        }
    }

    public void testCompilation() throws Exception {
        compile("$foo");
    }
    
    public void testVariableSelection() throws Exception {
        OXPath20ExpressionBPEL20 exp = compile("$foo");
        Node retVal = _runtime.evaluateNode(exp, this);
        assertNotNull(retVal);
        assertSame(retVal , _vars.get("foo"));
        assertSame(retVal.getOwnerDocument(),_vardoc);
    }

    public void testVariableSelectionEmpty() throws Exception {
        OXPath20ExpressionBPEL20 exp = compile("$emptyVar");
        Node retVal = _runtime.evaluateNode(exp, this);
        assertNotNull(retVal);
        assertSame(retVal , _vars.get("emptyVar"));
        assertTrue(DOMUtils.getFirstChildElement((Element)retVal).getLocalName().equals("empty"));
    }

    public void testVariableSelectionReallyEmpty() throws Exception {
        OXPath20ExpressionBPEL20 exp = compile("$reallyEmptyVar");
        Node retVal = _runtime.evaluateNode(exp, this);
        assertNotNull(retVal);
        assertSame(retVal , _vars.get("reallyEmptyVar"));
        assertNull(DOMUtils.getFirstChildElement((Element)retVal));
    }

    public Node readVariable(Variable variable, Part part) throws FaultException {
        return _vars.get(variable.name);
    }

    public Node getPartData(Element message, Part part) throws FaultException {
        // TODO Auto-generated method stub
        return null;
    }

    public String readMessageProperty(Variable variable, OProperty property) throws FaultException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isLinkActive(OLink olink) throws FaultException {
        // TODO Auto-generated method stub
        return false;
    }

    public Node getRootNode() {
        // TODO Auto-generated method stub
        return null;
    }

    public Node evaluateQuery(Node root, OExpression expr) throws FaultException {
        // TODO Auto-generated method stub
        return null;
    }

    public Long getProcessId() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean narrowTypes() {
        return true;
    }
    
    private OXPath20ExpressionBPEL20 compile(String xpath) {
        Document doc = DOMUtils.newDocument();
        Element e = doc.createElementNS(null, "expression");
        doc.appendChild(e);
        e.appendChild(doc.createTextNode(xpath));
        Expression exp = new Expression(e);
        return (OXPath20ExpressionBPEL20)_compiler.compileLValue(exp);
    }
    
    public URI getBaseResourceURI() {
        return null;
    }

    public Node getPropertyValue(QName propertyName) {
        return null;
    }

    public QName getProcessQName() {
        return null;
    }

    public Date getCurrentEventDateTime() {
        return null;
    }
}
