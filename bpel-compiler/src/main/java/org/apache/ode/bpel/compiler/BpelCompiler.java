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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.compiler.api.CompilationMessage;
import org.apache.ode.bpel.compiler.api.CompileListener;
import org.apache.ode.bpel.compiler.api.CompilerContext;
import org.apache.ode.bpel.compiler.api.ExpressionCompiler;
import org.apache.ode.bpel.compiler.api.ExpressionValidator;
import org.apache.ode.bpel.compiler.api.SourceLocation;
import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.Bpel11QNames;
import org.apache.ode.bpel.compiler.bom.Bpel20QNames;
import org.apache.ode.bpel.compiler.bom.BpelObject;
import org.apache.ode.bpel.compiler.bom.Catch;
import org.apache.ode.bpel.compiler.bom.CompensationHandler;
import org.apache.ode.bpel.compiler.bom.Correlation;
import org.apache.ode.bpel.compiler.bom.CorrelationSet;
import org.apache.ode.bpel.compiler.bom.Expression;
import org.apache.ode.bpel.compiler.bom.Expression11;
import org.apache.ode.bpel.compiler.bom.Extension;
import org.apache.ode.bpel.compiler.bom.FaultHandler;
import org.apache.ode.bpel.compiler.bom.Import;
import org.apache.ode.bpel.compiler.bom.LinkSource;
import org.apache.ode.bpel.compiler.bom.LinkTarget;
import org.apache.ode.bpel.compiler.bom.OnAlarm;
import org.apache.ode.bpel.compiler.bom.OnEvent;
import org.apache.ode.bpel.compiler.bom.PartnerLink;
import org.apache.ode.bpel.compiler.bom.PartnerLinkType;
import org.apache.ode.bpel.compiler.bom.Process;
import org.apache.ode.bpel.compiler.bom.Property;
import org.apache.ode.bpel.compiler.bom.PropertyAlias;
import org.apache.ode.bpel.compiler.bom.Scope;
import org.apache.ode.bpel.compiler.bom.ScopeActivity;
import org.apache.ode.bpel.compiler.bom.ScopeLikeActivity;
import org.apache.ode.bpel.compiler.bom.TerminationHandler;
import org.apache.ode.bpel.compiler.bom.Variable;
import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bpel.obj.DebugInfo;
import org.apache.ode.bpel.obj.OActivity;
import org.apache.ode.bpel.obj.OAssign;
import org.apache.ode.bpel.obj.OCatch;
import org.apache.ode.bpel.obj.OCompensate;
import org.apache.ode.bpel.obj.OConstantExpression;
import org.apache.ode.bpel.obj.OConstantVarType;
import org.apache.ode.bpel.obj.OConstants;
import org.apache.ode.bpel.obj.OElementVarType;
import org.apache.ode.bpel.obj.OEventHandler;
import org.apache.ode.bpel.obj.OExpression;
import org.apache.ode.bpel.obj.OExpressionLanguage;
import org.apache.ode.bpel.obj.OExtVar;
import org.apache.ode.bpel.obj.OFlow;
import org.apache.ode.bpel.obj.OLValueExpression;
import org.apache.ode.bpel.obj.OLink;
import org.apache.ode.bpel.obj.OMessageVarType;
import org.apache.ode.bpel.obj.OPartnerLink;
import org.apache.ode.bpel.obj.OProcess;
import org.apache.ode.bpel.obj.ORethrow;
import org.apache.ode.bpel.obj.OScope;
import org.apache.ode.bpel.obj.OSequence;
import org.apache.ode.bpel.obj.OVarType;
import org.apache.ode.bpel.obj.OXsdTypeVarType;
import org.apache.ode.bpel.obj.OXslSheet;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.NSContext;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.StreamUtils;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.stl.CollectionsX;
import org.apache.ode.utils.stl.MemberOfFunction;
import org.apache.ode.utils.stl.UnaryFunction;
import org.apache.ode.utils.xsd.SchemaModel;
import org.apache.ode.utils.xsd.XSUtils;
import org.apache.ode.utils.xsd.XsdException;
import org.apache.ode.utils.xsl.XslTransformHandler;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Compiler for converting BPEL process descriptions (and their associated WSDL and XSD documents) into compiled representations
 * suitable for execution by the ODE BPEL Service Provider. TODO: Move process validation into this class.
 */
public abstract class BpelCompiler implements CompilerContext {
    /** Class-severity logger. */
    protected static final Logger __log = LoggerFactory.getLogger(BpelCompiler.class);

    /** Standardized compiler messages. */
    private static final CommonCompilationMessages __cmsgs = MessageBundle.getMessages(CommonCompilationMessages.class);

    private org.apache.ode.bpel.compiler.bom.Process _processDef;

    private Date _generatedDate;

    @SuppressWarnings("rawtypes")
    private HashMap<Class, ActivityGenerator> _actGenerators = new HashMap<Class, ActivityGenerator>();

    private boolean _supressJoinFailure = false;

    /** Are we currently in an atomic scope context? */
    private boolean _atomicScope = false;

    /** Are we currently compiling an isolated scope context. */
    private boolean _isolatedScope = false;

    /** Syntactic scope stack. */
    private StructureStack _structureStack = new StructureStack();

    /** Fault/compensate recovery stack. */
    private Stack<OScope> _recoveryContextStack = new Stack<OScope>();

    /** History of compiled activities */
    private List<OActivity> _compiledActivities = new ArrayList<OActivity>();

    private OProcess _oprocess;

    private ResourceFinder _resourceFinder;

    private WSDLRegistry _wsdlRegistry;

    private final List<CompilationMessage> _errors = new ArrayList<CompilationMessage>();

    private CompileListener _compileListener;

    private final HashMap<String, ExpressionCompiler> _expLanguageCompilers = new HashMap<String, ExpressionCompiler>();

    private final HashMap<String, OExpressionLanguage> _expLanguages = new HashMap<String, OExpressionLanguage>();

    private ExpressionValidatorFactory _expressionValidatorFactory = new ExpressionValidatorFactory(System.getProperties());

    private WSDLFactory4BPEL _wsdlFactory;

    private OExpressionLanguage _konstExprLang;

    private Map<QName, Node> _customProcessProperties;

    private URI _processURI;

    private final Set<String> _declaredExtensionNS = new HashSet<String>();

    BpelCompiler(WSDLFactory4BPEL wsdlFactory) {
        _wsdlFactory = wsdlFactory;
        _wsdlRegistry = new WSDLRegistry(this);
    }

    public void addWsdlImport(URI from, URI wsdlImport, SourceLocation sloc) {
        Definition4BPEL def;
        try {
            WSDLReader r = _wsdlFactory.newWSDLReader();
            WSDLLocatorImpl locator = new WSDLLocatorImpl(_resourceFinder, _resourceFinder.resolve(from, wsdlImport));
            def = (Definition4BPEL) r.readWSDL(locator);
        } catch (WSDLException e) {
            recoveredFromError(sloc, new CompilationException(__cmsgs.errWsdlParseError(e.getFaultCode(), e.getLocation(), e
                    .getMessage())));
            throw new CompilationException(__cmsgs.errWsdlImportFailed(wsdlImport.toASCIIString(), e.getFaultCode())
                    .setSource(sloc), e);
        }

        try {
            _wsdlRegistry.addDefinition(def, _resourceFinder, _resourceFinder.resolve(from, wsdlImport));
            if (__log.isDebugEnabled())
                __log.debug("Added WSDL Definition: " + wsdlImport);
        } catch (CompilationException ce) {
            recoveredFromError(sloc, ce);
        }
    }

    public void addXsdImport(URI from, URI location, SourceLocation sloc) {
        URI resFrom = _resourceFinder.resolve(from, location);
        if (__log.isDebugEnabled())
            __log.debug("Adding XSD import from " + resFrom + " location " + location);
        XMLEntityResolver resolver = new WsdlFinderXMLEntityResolver(_resourceFinder,
                location, new HashMap<URI,byte[]>(), true);
        try {
            InputStream xsdStream = _resourceFinder.openResource(resFrom);
            byte[] data;
            try {
                data = StreamUtils.read(xsdStream);
            } finally {
                xsdStream.close();
            }

            Map<URI, byte[]> schemas = XSUtils.captureSchema(resFrom, data, resolver, 0);
            _wsdlRegistry.addSchemas(schemas);
        } catch (XsdException e) {
            CompilationException ce = new CompilationException(__cmsgs.errInvalidImport(location.toString()));
            recoveredFromError(sloc, ce);
        } catch (MalformedURLException e) {
            CompilationException ce = new CompilationException(__cmsgs.errInvalidImport(location.toString()));
            recoveredFromError(sloc, ce);
        } catch (IOException e) {
            CompilationException ce = new CompilationException(__cmsgs.errInvalidImport(location.toString()));
            recoveredFromError(sloc, ce);
        }
    }

    public void setResourceFinder(ResourceFinder finder) {
        if (finder == null) {
            _resourceFinder = new DefaultResourceFinder();
        } else {
            _resourceFinder = finder;
        }

    }

    public void setCompileListener(CompileListener compileListener) {
        _compileListener = compileListener;
    }

    public CompileListener getCompileListener() {
        return _compileListener;
    }

    public void setCustomProperties(Map<QName, Node> customProperties) {
        _customProcessProperties = customProperties;
    }

    /**
     * Get the process definition.
     *
     * @return the process definition
     */
    public Process getProcessDef() {
        return _processDef;
    }

    public PortType resolvePortType(final QName portTypeName) {
        if (portTypeName == null)
            throw new NullPointerException("Null portTypeName argument!");

        PortType portType = _wsdlRegistry.getPortType(portTypeName);
        if (portType == null)
            throw new CompilationException(__cmsgs.errUndeclaredPortType(portTypeName));
        return portType;
    }

    public OLink resolveLink(String linkName) {
        OLink ret = null;

        // Fist find where the link is declared.
        for (Iterator<OActivity> i = _structureStack.iterator(); i.hasNext();) {
            OActivity oact = i.next();
            if (oact instanceof OFlow)
                ret = ((OFlow) oact).getLocalLink(linkName);
            if (ret != null)
                return ret;
        }

        throw new CompilationException(__cmsgs.errUndeclaredLink(linkName));
    }

    public OScope.Variable resolveVariable(String varName) {
        for (Iterator<OScope> i = _structureStack.oscopeIterator(); i.hasNext();) {
            OScope.Variable var = i.next().getLocalVariable(varName);
            if (var != null)
                return var;
        }
        // A "real" variable couldn't be found, checking if we're dealing with a
        // process custom property
        if (_customProcessProperties != null && _customProcessProperties.get(QName.valueOf(varName)) != null) {

        }
        throw new CompilationException(__cmsgs.errUndeclaredVariable(varName));
    }

    public List<OScope.Variable> getAccessibleVariables() {
        ArrayList<OScope.Variable> result = new ArrayList<OScope.Variable>();
        for (Iterator<OScope> i = _structureStack.oscopeIterator(); i.hasNext();) {
            result.addAll(i.next().getVariables().values());
        }
        return result;
    }

    public OScope.Variable resolveMessageVariable(String inputVar) throws CompilationException {
        OScope.Variable var = resolveVariable(inputVar);
        if (!(var.getType() instanceof OMessageVarType))
            throw new CompilationException(__cmsgs.errMessageVariableRequired(inputVar));
        return var;
    }

    public OScope.Variable resolveMessageVariable(String inputVar, QName messageType) throws CompilationException {
        OScope.Variable var = resolveMessageVariable(inputVar);
        if (!((OMessageVarType) var.getType()).getMessageType().equals(messageType))
            throw new CompilationException(__cmsgs.errVariableTypeMismatch(var.getName(), messageType,
                    ((OMessageVarType) var.getType()).getMessageType()));
        return var;
    }

    public OProcess.OProperty resolveProperty(QName name) {

        for (OProcess.OProperty prop : _oprocess.getProperties()) {
            if (prop.getName().equals(name))
                return prop;
        }
        throw new CompilationException(__cmsgs.errUndeclaredProperty(name));
    }

    public OProcess.OPropertyAlias resolvePropertyAlias(OScope.Variable variable, QName propertyName) {
        if (!(variable.getType() instanceof OMessageVarType))
            throw new CompilationException(__cmsgs.errMessageVariableRequired(variable.getName()));

        OProcess.OProperty property = resolveProperty(propertyName);
        OProcess.OPropertyAlias alias = property.getAlias(variable.getType());
        if (alias == null)
            throw new CompilationException(__cmsgs.errUndeclaredPropertyAlias(variable.getType().toString(), propertyName));

        return alias;
    }

    public OScope resolveCompensatableScope(final String scopeToCompensate) throws CompilationException {
        if (_recoveryContextStack.isEmpty())
            throw new CompilationException(__cmsgs.errCompensateNAtoContext());
        OScope recoveryContext = _recoveryContextStack.peek();

        OScope scopeToComp = CollectionsX.find_if(recoveryContext.getCompensatable(), new MemberOfFunction<OScope>() {
            public boolean isMember(OScope o) {
                return o.getName() != null && o.getName().equals(scopeToCompensate);
            }
        });
        if (scopeToComp == null)
            throw new CompilationException(__cmsgs.errCompensateOfInvalidScope(scopeToCompensate));

        return scopeToComp;
    }

    public String getSourceLocation() {
        return _processDef.getSource() == null ? null : _processDef.getSource();
    }

    public OScope.CorrelationSet resolveCorrelationSet(String csetName) {
        for (Iterator<OScope> i = _structureStack.oscopeIterator(); i.hasNext();) {
            OScope.CorrelationSet cset = i.next().getCorrelationSet(csetName);
            if (cset != null)
                return cset;
        }

        throw new CompilationException(__cmsgs.errUndeclaredCorrelationSet(csetName));
    }

    @SuppressWarnings("unchecked")
    public OMessageVarType resolveMessageType(QName messageType) {
        OMessageVarType msgType = _oprocess.getMessageTypes().get(messageType);
        if (msgType == null) {
            Message msg = _wsdlRegistry.getMessage(messageType);
            if (msg == null) {
                throw new CompilationException(__cmsgs.errUndeclaredMessage(messageType.getLocalPart(), messageType
                        .getNamespaceURI()));
            }

            List<OMessageVarType.Part> parts = new ArrayList<OMessageVarType.Part>();
            CollectionsX.transform(parts, ((List<Part>) msg.getOrderedParts(null)),
                    new UnaryFunction<Part, OMessageVarType.Part>() {
                        public OMessageVarType.Part apply(Part part) {
                            OVarType partType;
                            if (part.getElementName() != null) {
                                partType = resolveElementType(part.getElementName());
                            } else {
                                partType = resolveXsdType(part.getTypeName());
                            }

                            OMessageVarType.Part opart = new OMessageVarType.Part(_oprocess, part.getName(), partType);
                            opart.setDebugInfo(createDebugInfo(_processDef, ("Message Variable Part: " + (part.getName()))));
                            return opart;
                        }
                    });
            msgType = new OMessageVarType(_oprocess, msg.getQName(), parts);
            msgType.setDebugInfo(createDebugInfo(_processDef, ("Message Type: " + (msg.getQName()))));
            _oprocess.getMessageTypes().put(msg.getQName(), msgType);
        }
        return msgType;
    }

    public OXsdTypeVarType resolveXsdType(QName typeName) throws CompilationException {
        OXsdTypeVarType type = _oprocess.getXsdTypes().get(typeName);
        if (type == null) {
            __log.debug("Resolving XSD type " + typeName);
            SchemaModel model = null;
            try {
                model = _wsdlRegistry.getSchemaModel();
            } catch (IllegalArgumentException iaa) { }
            if (model == null || !model.knowsSchemaType(typeName))
                throw new CompilationException(__cmsgs.errUndeclaredXsdType(typeName));

            type = new OXsdTypeVarType(_oprocess);
            type.setDebugInfo(createDebugInfo(_processDef, ("XSD Type: " + typeName)));
            type.setXsdType(typeName);
            type.setSimple(_wsdlRegistry.getSchemaModel().isSimpleType(typeName));
            _oprocess.getXsdTypes().put(typeName, type);
        }

        return type;
    }

    public OMessageVarType.Part resolvePart(OScope.Variable var, String partname) {
        if (!(var.getType() instanceof OMessageVarType))
            throw new CompilationException(__cmsgs.errMessageVariableRequired(var.getName()));
        OMessageVarType msgVarType = (OMessageVarType) var.getType();
        OMessageVarType.Part part = msgVarType.getParts().get(partname);
        if (part == null)
            throw new CompilationException(__cmsgs.errUndeclaredMessagePart(var.getName(), ((OMessageVarType) var.getType()).getMessageType(),
                    partname));
        return part;
    }

    public OMessageVarType.Part resolveHeaderPart(OScope.Variable var, String partname) {
        if (!(var.getType() instanceof OMessageVarType))
            throw new CompilationException(__cmsgs.errMessageVariableRequired(var.getName()));
        OMessageVarType msgVarType = (OMessageVarType) var.getType();
        return msgVarType.getParts().get(partname);
    }

    public PartnerLinkType resolvePartnerLinkType(QName partnerLinkType) {

        PartnerLinkType plinkType = _wsdlRegistry.getPartnerLinkType(partnerLinkType);
        if (plinkType == null)
            throw new CompilationException(__cmsgs.errUndeclaredPartnerLinkType(partnerLinkType));
        return plinkType;
    }

    public OPartnerLink resolvePartnerLink(String name) {
        for (Iterator<OScope> i = _structureStack.oscopeIterator(); i.hasNext();) {
            OPartnerLink oplink = i.next().getLocalPartnerLink(name);
            if (oplink != null)
                return oplink;
        }

        throw new CompilationException(__cmsgs.errUndeclaredPartnerLink(name));
    }

    @SuppressWarnings("unchecked")
    public Operation resolvePartnerRoleOperation(final OPartnerLink partnerLink, final String operationName) {
        if (partnerLink.getPartnerRolePortType() == null) {
            throw new CompilationException(__cmsgs.errPartnerLinkDoesNotDeclarePartnerRole(partnerLink.getName()));
        }

        Operation found = CollectionsX.find_if((List<Operation>) partnerLink.getPartnerRolePortType().getOperations(),
                new MemberOfFunction<Operation>() {
                    public boolean isMember(Operation o) {
                        // Guard against WSDL4j funny business.
                        if ((o.getInput() == null || o.getInput().getMessage() == null)
                                && (o.getOutput() == null || o.getOutput().getMessage() == null)) {
                            return false;
                        }
                        return o.getName().equals(operationName);
                    }
                });

        if (found == null)
            throw new CompilationException(__cmsgs
                    .errUndeclaredOperation(partnerLink.getPartnerRolePortType().getQName(), operationName));
        return found;
    }

    @SuppressWarnings("unchecked")
    public Operation resolveMyRoleOperation(final OPartnerLink partnerLink, final String operationName) {
        if (partnerLink.getMyRolePortType() == null) {
            throw new CompilationException(__cmsgs.errPartnerLinkDoesNotDeclareMyRole(partnerLink.getName()));
        }

        Operation found = CollectionsX.find_if((List<Operation>) partnerLink.getMyRolePortType().getOperations(),
                new MemberOfFunction<Operation>() {
                    public boolean isMember(Operation o) {
                        // Again, guard against WSDL4J's "help"
                        if ((o.getInput() == null || o.getInput().getMessage() == null)
                                && (o.getOutput() == null || o.getOutput().getMessage() == null))
                            return false;
                        return o.getName().equals(operationName);
                    }
                });
        if (found == null) {
            throw new CompilationException(__cmsgs.errUndeclaredOperation(partnerLink.getMyRolePortType().getQName(), operationName));
        }
        return found;
    }

    /**
     * Produce a boolean {@link OExpression} expression that returns a constant
     * value.
     *
     * @param value
     *            constant value to return
     * @return {@link OExpression} returning a constant value.
     */
    public OExpression constantExpr(boolean value) {
        OConstantExpression ce = new OConstantExpression(_oprocess, value ? Boolean.TRUE : Boolean.FALSE);
        ce.setDebugInfo(createDebugInfo(_processDef, ("Constant Boolean Expression: " + value)));
        ce.setExpressionLanguage(_konstExprLang);
        return ce;
    }

    public OLValueExpression compileLValueExpr(Expression expression) throws CompilationException {
        return compileLValueExpr(expression, null, null, new Object[1]);
    }

    public OLValueExpression compileLValueExpr(Expression expression, OVarType rootNodeType, Object requestedResultType, Object[] resultType) throws CompilationException {
        return (OLValueExpression) compileExpr(expression, false, true, rootNodeType, requestedResultType, resultType);
    }

    public OExpression compileJoinCondition(Expression expression) throws CompilationException {
        return compileExpr(expression, true, false, null, null, new Object[1]);
    }

    public OExpression compileExpr(Expression expression) throws CompilationException {
        return compileExpr(expression, null, null, new Object[1]);
    }

    public OExpression compileExpr(Expression expression, OVarType rootNodeType, Object requestedResultType, Object[] resultType) throws CompilationException {
        return compileExpr(expression, false, false, rootNodeType, requestedResultType, resultType);
    }

    public OExpression compileExpr(String expr, NSContext nc) {
        // Does this really work?
        BpelObject cur = _structureStack.topSource();
        return compileExpr(new Expression11(cur.getElement(),cur.getElement().getOwnerDocument().createTextNode(expr)), false, false, null, null, new Object[1]);
    }

    private OExpression compileExpr(Expression expression, boolean isJoinCondition, boolean isLValue, OVarType rootNodeType, Object requestedResultType, Object[] resultType) {
        String expLang = getExpressionLanguage(expression);
        ExpressionCompiler ec = findExpLangCompiler(expLang);
        ec.setCompilerContext(this);
        ExpressionValidator ev = _expressionValidatorFactory.getValidator();

        try {
            OExpression oexpr;
            if (isJoinCondition) {
                oexpr = ec.compileJoinCondition(expression);
            } else {
                oexpr = ec.compile(expression);
                resultType[0] = ev.validate(expression, rootNodeType, requestedResultType);
            }

            oexpr.setDebugInfo(createDebugInfo(expression, expression.toString()));

            OExpressionLanguage expLanguage = _expLanguages.get(expLang);
            if (expLanguage == null) {
                expLanguage = new OExpressionLanguage(_oprocess, ec.getProperties());
                expLanguage.setDebugInfo(createDebugInfo(_processDef, ("Expression Language: " + expLang)));
                expLanguage.setExpressionLanguageUri(expLang);
                _expLanguages.put(expLang, expLanguage);
                _oprocess.getExpressionLanguages().add(expLanguage);
            }
            oexpr.setExpressionLanguage(expLanguage);

            // Cleaning up expression compiler for furter compilation
            ec.setCompilerContext(null);

            return oexpr;
        } catch (CompilationException ce) {
            if (ce.getCompilationMessage().source == null)
                ce.getCompilationMessage().setSource(expression);
            throw ce;
        }
    }

    public OProcess getOProcess() throws CompilationException {
        return _oprocess;
    }

    public void recoveredFromError(SourceLocation where, CompilationException bce) throws CompilationException {
        if (bce.getCompilationMessage().source == null)
            bce.getCompilationMessage().source = where;

        if (_compileListener == null) {
            switch (bce.getCompilationMessage().severity) {
            case CompilationMessage.INFO:
                if (__log.isInfoEnabled()) {
                    __log.info(bce.toErrorMessage());
                }
                break;
            case CompilationMessage.WARN:
                if (__log.isWarnEnabled()) {
                    __log.warn(bce.toErrorMessage());
                }
                break;
            case CompilationMessage.ERROR:
                if (__log.isErrorEnabled()) {
                    __log.error(bce.toErrorMessage());
                }
            }
        } else {
            if (__log.isDebugEnabled()) {
                __log.debug(bce.toErrorMessage(), bce);
            }
            _compileListener.onCompilationMessage(bce.getCompilationMessage());
        }

        _errors.add(bce.getCompilationMessage());
    }

    public static long getVersion(String dirName) {
        try {
            return Integer.parseInt(dirName.substring(dirName.lastIndexOf("-") + 1));
        } catch (Throwable t) {
            return 0;
        }
    }

    /**
     * Compile a process.
     */
    public OProcess compile(final Process process, ResourceFinder rf, long version) throws CompilationException {
        if (process == null)
            throw new NullPointerException("Null process parameter");

        setResourceFinder(rf);
        _processURI = process.getURI();
        _processDef = process;
        _generatedDate = new Date();
        _structureStack.clear();

        String bpelVersionUri = null;
        switch (process.getBpelVersion()) {
        case BPEL11:
            bpelVersionUri = Bpel11QNames.NS_BPEL4WS_2003_03;
            break;
        case BPEL20_DRAFT:
            bpelVersionUri = Bpel20QNames.NS_WSBPEL2_0;
            break;
        case BPEL20:
            bpelVersionUri = Bpel20QNames.NS_WSBPEL2_0_FINAL_EXEC;
            break;
        default:
            throw new IllegalStateException("Bad bpel version: " + process.getBpelVersion());
        }

        _oprocess = new OProcess(bpelVersionUri);
        _oprocess.setGuid(null);
        _oprocess.setConstants(makeConstants());
        _oprocess.setDebugInfo(createDebugInfo(process, "process"));
        _oprocess.setNamespaceContext(process.getNamespaceContext());
        
        if (process.getTargetNamespace() == null) {
            _oprocess.setTargetNamespace("--UNSPECIFIED--");
            recoveredFromError(process, new CompilationException(__cmsgs.errProcessNamespaceNotSpecified()));
        } else {
            _oprocess.setTargetNamespace(_processDef.getTargetNamespace());
        }

        if (process.getName() == null) {
            _oprocess.setProcessName("--UNSPECIFIED--");
            recoveredFromError(process, new CompilationException(__cmsgs.errProcessNameNotSpecified()));
        } else {
            _oprocess.setProcessName(_processDef.getName());
        }

        _oprocess.setCompileDate(_generatedDate);

        _konstExprLang = new OExpressionLanguage(_oprocess, null);
        _konstExprLang.setDebugInfo(createDebugInfo(_processDef, "Constant Value Expression Language"));
        _konstExprLang.setExpressionLanguageUri("uri:www.fivesight.com/konstExpression");
        _konstExprLang.getProperties().put("runtime-class",
                "org.apache.ode.bpel.runtime.explang.konst.KonstExpressionLanguageRuntimeImpl");
        _oprocess.getExpressionLanguages().add(_konstExprLang);

        // Process the imports. Note, we expect all processes (Event BPEL 1.1)
        // to have an import declaration. This should be automatically generated
        // by the 1.1 parser.
        for (Import imprt : _processDef.getImports()) {
            try {
                compile(_processURI, imprt);
            } catch (CompilationException bce) {
                // We try to recover from import problems by continuing
                recoveredFromError(imprt, bce);
            }
        }

        _expressionValidatorFactory.getValidator().bpelImportsLoaded(_processDef, this);

        switch (_processDef.getSuppressJoinFailure()) {
        case NO:
        case NOTSET:
            _supressJoinFailure = false;
            break;
        case YES:
            _supressJoinFailure = true;
            break;
        }
        // compile ALL wsdl properties; needed for property extraction
        Definition4BPEL[] defs = _wsdlRegistry.getDefinitions();
        for (Definition4BPEL def : defs) {
            for (Property property : def.getProperties()) {
                compile(property);
            }
        }
        // compile ALL wsdl property aliases
        for (Definition4BPEL def1 : defs) {
            for (PropertyAlias propertyAlias : def1.getPropertyAliases()) {
                compile(propertyAlias);
            }
        }

        // compile extensions
        for (Extension e : _processDef.getExtensions()) {
            compileExtension(e);
        }

        OScope procesScope = new OScope(_oprocess, null);
        procesScope.setName("__PROCESS_SCOPE:" + (process.getName()));
        procesScope.setDebugInfo(createDebugInfo(process, null));
        _oprocess.setProcesScope(compileScope(procesScope, process, new java.lang.Runnable() {
    public void run() {
    	 if (process.getRootActivity() == null) {
             throw new CompilationException(__cmsgs.errNoRootActivity());
         }
         // Process custom properties are created as variables associated
         // with the top scope
         if (_customProcessProperties != null) {
             for (Map.Entry<QName, Node> customVar : _customProcessProperties.entrySet()) {
                 final OScope oscope = _structureStack.topScope();
                 OVarType varType = new OConstantVarType(_oprocess, customVar.getValue());
                 OScope.Variable ovar = new OScope.Variable(_oprocess, varType);
                 ovar.setName(customVar.getKey().getLocalPart());
                 ovar.setDeclaringScope(oscope);
                 ovar.setDebugInfo(createDebugInfo(null, "Process custom property variable"));
                 oscope.addLocalVariable(ovar);
                 if (__log.isDebugEnabled())
                     __log.debug("Compiled custom property variable " + ovar);
             }
         }
         _structureStack.topScope().setActivity(compile(process.getRootActivity()));
     }
 }));


        assert _structureStack.size() == 0;

        boolean hasErrors = false;
        StringBuffer sb = new StringBuffer();
        for (CompilationMessage msg : _errors) {
            if (msg.severity >= CompilationMessage.ERROR) {
                hasErrors = true;
                sb.append('\t');
                sb.append(msg.toErrorString());
                sb.append('\n');
            }
        }

        XslTransformHandler.getInstance().clearXSLSheets(_oprocess.getQName());

        _expressionValidatorFactory.getValidator().bpelCompilationCompleted(_processDef);

        if (hasErrors) {
            throw new CompilationException(__cmsgs.errCompilationErrors(_errors.size(), sb.toString()));
        }

        {
            String digest = "version:" + version + ";" + _oprocess.digest();
            _oprocess.setGuid(org.apache.ode.utils.GUID.makeGUID(digest));
            if (__log.isDebugEnabled()) {
                __log.debug("Compiled process digest: " + digest + "\nguid: " + _oprocess.getGuid());
            }
        }
        return _oprocess;
    }

    private OConstants makeConstants() {
        OConstants constants = new OConstants(_oprocess);
        constants.setQnConflictingReceive(new javax.xml.namespace.QName(getBpwsNamespace() , "conflictingReceive"));
        constants.setQnConflictingRequest(new javax.xml.namespace.QName(getBpwsNamespace() , "conflictingRequest"));
        constants.setQnCorrelationViolation(new javax.xml.namespace.QName(getBpwsNamespace() , "correlationViolation"));
        constants.setQnForcedTermination(new javax.xml.namespace.QName(getBpwsNamespace() , "forcedTermination"));
        constants.setQnJoinFailure(new javax.xml.namespace.QName(getBpwsNamespace() , "joinFailure"));
        constants.setQnMismatchedAssignmentFailure(new javax.xml.namespace.QName(getBpwsNamespace() , "mismatchedAssignment"));
        constants.setQnMissingReply(new javax.xml.namespace.QName(getBpwsNamespace() , "missingReply"));
        constants.setQnMissingRequest(new javax.xml.namespace.QName(getBpwsNamespace() , "missingRequest"));
        constants.setQnSelectionFailure(new javax.xml.namespace.QName(getBpwsNamespace() , "selectionFailure"));
        constants.setQnUninitializedVariable(new javax.xml.namespace.QName(getBpwsNamespace() , "uninitializedVariable"));
        constants.setQnXsltInvalidSource(new javax.xml.namespace.QName(getBpwsNamespace() , "xsltInvalidSource"));
        constants.setQnSubLanguageExecutionFault(new javax.xml.namespace.QName(getBpwsNamespace() , "subLanguageExecutionFault"));
        constants.setQnUninitializedPartnerRole(new javax.xml.namespace.QName(getBpwsNamespace() , "uninitializedPartnerRole"));
        constants.setQnForEachCounterError(new javax.xml.namespace.QName(getBpwsNamespace() , "forEachCounterError"));
        constants.setQnInvalidBranchCondition(new javax.xml.namespace.QName(getBpwsNamespace() , "invalidBranchCondition"));
        constants.setQnInvalidExpressionValue(new javax.xml.namespace.QName(getBpwsNamespace() , "invalidExpressionValue"));

        constants.setQnRetiredProcess(new javax.xml.namespace.QName(getOdeNamespace() , "retiredProcess"));
        constants.setQnTooManyInstances(new javax.xml.namespace.QName(getOdeNamespace() , "tooManyInstances"));
        constants.setQnUnknownFault(new javax.xml.namespace.QName(getOdeNamespace() , "unknownFault"));
        constants.setQnTooManyProcesses(new javax.xml.namespace.QName(getOdeNamespace() , "tooManyProcesses"));
        constants.setQnTooHugeProcesses(new javax.xml.namespace.QName(getOdeNamespace() , "tooHugeProcesses"));
        return constants;
    }

    private String getOdeNamespace() {
        return Namespaces.ODE_EXTENSION_NS;
    }

    // TODO unused?
    // private String getBpelPartnerLinkUri(){
    // switch(_processDef.getBpelVersion()){
    // case Process.BPEL_V110:
    // return Constants.NS_BPEL4WS_PARTNERLINK_2003_05;
    // case Process.BPEL_V200:
    // return Constants.NS_WSBPEL_PARTNERLINK_2004_03;
    // default:
    // throw new IllegalStateException("Bad bpel version.");
    // }
    // }

    /**
     * Compile an import declaration. According to the specification:
     * <blockquote> A BPEL4WSWS-BPEL process definition relies on XML Schema and
     * WSDL 1.1 for the definition of datatypes and service interfaces. Process
     * definitions also rely on other constructs such as partner link types,
     * message properties and property aliases (defined later in this
     * specification) which are defined within WSDL 1.1 documents using the WSDL
     * 1.1 language extensibility feature.
     *
     * The &lt;import&gt; element is used within a BPEL4WSWS-BPEL process to
     * explicitly indicate a dependency on external XML Schema or WSDL
     * definitions. Any number of <import> elements may appear as initial
     * children of the <process> element, before any other child element. Each
     * <import> element contains three mandatory attributes:
     * <ol>
     * <li>namespace -- The namespace attribute specifies the URI namespace of
     * the imported definitions. </li>
     * <li>location -- The location attribute contains a URI indicating the
     * location of a document that contains relevant definitions in the
     * namespace specified. The document located at the URI MUST contain
     * definitions belonging to the same namespace as indicated by the namespace
     * attribute. </li>
     * <li>importType -- The importType attribute identifies the type of
     * document being imported by providing the URI of the encoding language.
     * The value MUST be set to "http://www.w3.org/2001/XMLSchema" when
     * importing XML Schema 1.0 documents, and to
     * "http://schemas.xmlsoap.org/wsdl/" when importing WSDL 1.1 documents.
     *
     * @param imprt
     *            BOM representation of the import
     */
    private void compile(URI current, Import imprt) {
        try {
            if (imprt.getImportType() == null)
                throw new CompilationException(__cmsgs.errUnspecifiedImportType().setSource(imprt));

            if (imprt.getLocation() == null)
                throw new CompilationException(__cmsgs.errMissingImportLocation().setSource(imprt));

            if (Import.IMPORTTYPE_WSDL11.equals(imprt.getImportType())) {
                addWsdlImport(current, imprt.getLocation(), imprt);
            } else if (Import.IMPORTTYPE_XMLSCHEMA10.equals(imprt.getImportType())) {
                addXsdImport(current, imprt.getLocation(), imprt);
            } else
                throw new CompilationException(__cmsgs.errUnknownImportType(imprt.getImportType()).setSource(imprt));
        } catch (CompilationException ce) {
            if (ce.getCompilationMessage().source == null)
                ce.getCompilationMessage().setSource(imprt);
            throw ce;
        }
    }

    public OActivity compile(final Activity source) {
        if (source == null)
            throw new IllegalArgumentException("null-argument");

        boolean previousSupressJoinFailure = _supressJoinFailure;
        switch (source.getSuppressJoinFailure()) {
        case NO:
            _supressJoinFailure = false;
            break;
        case YES:
            _supressJoinFailure = true;
            break;
        }

        OActivity compiled;
        try {
            compiled = (source instanceof ScopeLikeActivity) ? compileSLC((ScopeLikeActivity) source, new OScope.Variable[0])
                    : compileActivity(true, source);
            compiled.setSuppressJoinFailure(_supressJoinFailure);
        } finally {
            _supressJoinFailure = previousSupressJoinFailure;
        }

        if (__log.isDebugEnabled())
            __log.debug("Compiled activity " + compiled);
        return compiled;
    }

    private OCompensate createDefaultCompensateActivity(BpelObject source, String desc) {
        OCompensate activity = new OCompensate(_oprocess, getCurrent());
        activity.setName("__autoGenCompensate:" + (_structureStack.topScope().getName()));
        activity.setDebugInfo(createDebugInfo(source, desc));
        return activity;
    }

    public OScope compileSLC(final ScopeLikeActivity source, final OScope.Variable[] variables) {
        final OScope implicitScope = new OScope(_oprocess, getCurrent());
        implicitScope.setImplicitScope(true);
        implicitScope.setName(createName(source, "implicit-scope"));
        implicitScope.setDebugInfo(createDebugInfo(source, ("Scope-like construct " + source)));
        compileScope(implicitScope, source.getScope(), new Runnable() {
            public void run() {
                compileLinks(source);
                for (OScope.Variable v : variables) {
                    v.setDeclaringScope(implicitScope);
                    implicitScope.addLocalVariable(v);
                }
                if (source instanceof ScopeActivity) {
                    Activity scopeChild = ((ScopeActivity) source).getChildActivity();
                    if (scopeChild == null)
                        throw new CompilationException(__cmsgs.errEmptyScope().setSource(source));
                    implicitScope.setActivity(compile(scopeChild));
                } else {
                    implicitScope.setActivity(compileActivity(false, source));
                }
            }
        });

        return implicitScope;
    }

    private OActivity compileActivity(final boolean doLinks, final Activity source) {
        final ActivityGenerator actgen = findActivityGen(source);
        final OActivity oact = actgen.newInstance(source);
        oact.setName(createName(source, "activity"));
        oact.setDebugInfo(createDebugInfo(source, ("Activity body for " + source)));
        _compiledActivities.add(oact);
        compile(oact, source, new Runnable() {
            public void run() {
                if (doLinks)
                    compileLinks(source);
                actgen.compile(oact, source);
            }
        });

        return oact;
    }

    private void compileLinks(Activity source) {
        /* Source Links Fixup */
        for (LinkSource ls : source.getLinkSources())
            compileLinkSource(ls);

        /* Target Links Fixup */
        for (LinkTarget lt : source.getLinkTargets())
            compileLinkTarget(lt);

        _structureStack.topActivity().setJoinCondition(((source.getJoinCondition()) == null) || (source.getLinkTargets().isEmpty()) ? null : compileJoinCondition(source.getJoinCondition()));
    }

    private String createName(Activity source, String type) {
        if (source.getName() != null)
            return source.getName();

        return source.getType().getLocalPart() + "-" + type + "-line-" + source.getLineNo();
    }

    private OProcess.OProperty compile(Property property) {
        OProcess.OProperty oproperty = new OProcess.OProperty(_oprocess);
        oproperty.setName(property.getName());
        oproperty.setDebugInfo(createDebugInfo(_processDef, ("Property " + (property.getName()))));

        if (!_wsdlRegistry.getSchemaModel().isSimpleType(property.getPropertyType()))
            throw new CompilationException(__cmsgs.errPropertyDeclaredWithComplexType(property.getName(),
                    property.getPropertyType()).setSource(property));

        _oprocess.getProperties().add(oproperty);

        if (__log.isDebugEnabled())
            __log.debug("Compiled property " + oproperty);

        return oproperty;
    }

    private OProcess.OPropertyAlias compile(PropertyAlias src) {
        OProcess.OProperty property = resolveProperty(src.getPropertyName());

        OProcess.OPropertyAlias alias = new OProcess.OPropertyAlias(_oprocess);
        alias.setDebugInfo(createDebugInfo(_processDef, ((("PropertyAlias " + (src.getPropertyName())) + " for ") + (src.getMessageType()))));
        if (src.getMessageType() == null) {
            throw new CompilationException(__cmsgs.errAliasUndeclaredMessage(src.getPropertyName(), src.getQuery().getPath()));
        }

        OMessageVarType messageType = resolveMessageType(src.getMessageType());
        OVarType rootNodeType = messageType;
        alias.setVarType(messageType);
        // bpel 2.0 excludes declaration of part;
        // bpel 1.1 requires it
        if (src.getPart() != null) {
            alias.setPart(messageType.getParts().get(src.getPart()));
            if (alias.getPart() == null)
                throw new CompilationException(__cmsgs.errUnknownPartInAlias(src.getPart(),
                        messageType.getMessageType().toString()));
            rootNodeType = alias.getPart().getType();
        } else if (src.getHeader() != null) {
            alias.setHeader(src.getHeader());
            rootNodeType = new OElementVarType(_oprocess, QName.valueOf("{http://www.w3.org/2001/XMLSchema}any"));
        }
        if (src.getQuery() != null)
            alias.setLocation(compileExpr(src.getQuery(), rootNodeType, null, new java.lang.Object[1]));
        property.getAliases().add(alias);
        alias.setDebugInfo(createDebugInfo(_processDef, (((src.getMessageType()) + " --> ") + (src.getPropertyName()))));
        return alias;
    }

    private void compileLinkTarget(LinkTarget target) {
        OLink ol = resolveLink(target.getLinkName());
        assert ol != null;
        ol.setDebugInfo(createDebugInfo(target, target.toString()));
        if (ol.getTarget() != null)
            throw new CompilationException(__cmsgs.errDuplicateLinkTarget(target.getLinkName()).setSource(target));
        ol.setTarget(_structureStack.topActivity());

        _structureStack.topActivity().getTargetLinks().add(ol);
    }

    private void compileLinkSource(LinkSource linksrc) {
        OLink ol = resolveLink(linksrc.getLinkName());
        assert ol != null;
        ol.setDebugInfo(createDebugInfo(linksrc, linksrc.toString()));
        if (ol.getSource() != null)
            throw new CompilationException(__cmsgs.errDuplicateLinkSource(linksrc.getLinkName()).setSource(linksrc));
        ol.setSource(_structureStack.topActivity());
        ol.setTransitionCondition((linksrc.getTransitionCondition()) == null ? constantExpr(true) : compileExpr(linksrc.getTransitionCondition()));

        _structureStack.topActivity().getSourceLinks().add(ol);
        _structureStack.topActivity().getOutgoingLinks().add(ol);
    }

    private void compile(final PartnerLink plink) {
        OPartnerLink oplink = new OPartnerLink(_oprocess);
        oplink.setDebugInfo(createDebugInfo(plink, plink.toString()));
        try {
            PartnerLinkType plinkType = resolvePartnerLinkType(plink.getPartnerLinkType());

            oplink.setPartnerLinkType(plinkType.getName());
            oplink.setName(plink.getName());
            oplink.setInitializePartnerRole(plink.isInitializePartnerRole());

            if (plink.hasMyRole()) {
                PartnerLinkType.Role myRole = plinkType.getRole(plink.getMyRole());
                if (myRole == null)
                    throw new CompilationException(__cmsgs.errUndeclaredRole(plink.getMyRole(), plinkType.getName()));
                oplink.setMyRoleName(myRole.getName());
                QName portType = myRole.getPortType();
                if (portType == null)
                    throw new CompilationException(__cmsgs.errMissingMyRolePortType(myRole.getPortType(), plink.getMyRole(), plinkType.getName()));
                oplink.setMyRolePortType(resolvePortType(portType));
            }

            if (plink.isInitializePartnerRole() && !plink.hasPartnerRole()) {
                throw new CompilationException(__cmsgs.errPartnerLinkNoPartnerRoleButInitialize(plink.getName()));
            }
            if (plink.hasPartnerRole()) {
                PartnerLinkType.Role partnerRole = plinkType.getRole(plink.getPartnerRole());
                if (partnerRole == null)
                    throw new CompilationException(__cmsgs.errUndeclaredRole(plink.getPartnerRole(), plinkType.getName()));
                oplink.setPartnerRoleName(partnerRole.getName());
                QName portType = partnerRole.getPortType();
                if (portType == null)
                    throw new CompilationException(__cmsgs.errMissingPartnerRolePortType(partnerRole.getPortType(), plink.getPartnerRole(), plinkType.getName()));
                oplink.setPartnerRolePortType(resolvePortType(portType));
            }

            oplink.setDeclaringScope(_structureStack.topScope());
            if (oplink.getDeclaringScope().getPartnerLinks().containsKey(oplink.getName()))
                throw new CompilationException(__cmsgs.errDuplicatePartnerLinkDecl(oplink.getName()));
            oplink.getDeclaringScope().getPartnerLinks().put(oplink.getName(), oplink);
            _oprocess.getAllPartnerLinks().add(oplink);
        } catch (CompilationException ce) {
            ce.getCompilationMessage().setSource(plink);
            throw ce;
        }
    }

    private void compile(CorrelationSet cset) {
        OScope oscope = _structureStack.topScope();
        OScope.CorrelationSet ocset = new OScope.CorrelationSet(_oprocess);
        ocset.setName(cset.getName());
        ocset.setDeclaringScope(oscope);
        ocset.setDebugInfo(createDebugInfo(cset, cset.toString()));
        QName[] setprops = cset.getProperties();
        for (int j = 0; j < setprops.length; ++j)
            ocset.getProperties().add(resolveProperty(setprops[j]));
        oscope.addCorrelationSet(ocset);
    }

    public OActivity getCurrent() {
        return _structureStack.topActivity();
    }

    public void compile(OActivity context, BpelObject source, Runnable run) {
        DefaultActivityGenerator.defaultExtensibilityElements(context, source);
        _structureStack.push(context, source);
        try {
            run.run();
        } finally {
            OActivity popped = _structureStack.pop();

            OActivity newtop = _structureStack.topActivity();
            OScope topScope = _structureStack.topScope();

            if (newtop != null) {
                newtop.getNested().add(popped);
                // Transfer outgoing and incoming links, excluding the locally defined links.
                newtop.getIncomingLinks().addAll(popped.getIncomingLinks());
                if (newtop instanceof OFlow) newtop.getIncomingLinks().removeAll(((OFlow) newtop).getLocalLinks());
                newtop.getOutgoingLinks().addAll(popped.getOutgoingLinks());

                if (newtop instanceof OFlow) newtop.getOutgoingLinks().removeAll(((OFlow) newtop).getLocalLinks());

                // Transfer variables read/writen
                newtop.getVariableRd().addAll(popped.getVariableRd());
                newtop.getVariableWr().addAll(popped.getVariableWr());
            }

            if (topScope != null && popped instanceof OScope) topScope.getCompensatable().add((OScope) popped);
        }
    }

    private OScope compileScope(final OScope oscope, final Scope src, final Runnable init) {
        if (oscope.getName() == null)
            throw new IllegalArgumentException("Unnamed scope:" + src);

        oscope.setDebugInfo(createDebugInfo(src, src.toString()));

        //TODO: Is the logic incorrect? 
        boolean previousAtomicScope = _atomicScope;
        if (src.getAtomicScope() != null) {
            boolean newValue = src.getAtomicScope().booleanValue();
            if (_atomicScope)
                throw new CompilationException(__cmsgs.errAtomicScopeNesting(newValue));

            oscope.setAtomicScope(_atomicScope = newValue);
        }

        if (src.getIsolatedScope() != null) {
            if (src.getIsolatedScope()) {
                if (_isolatedScope)
                    throw new CompilationException(__cmsgs.errIsolatedScopeNesting());

                oscope.setIsolatedScope(_isolatedScope = true);
            }
        }

        try {
            compile(oscope, src, new Runnable() {
                public void run() {
                    for (Variable var : src.getVariables()) {
                        try {
                            compile(var);
                        } catch (CompilationException ce) {
                            recoveredFromError(var, ce);
                        }
                    }

                    for (CorrelationSet cset : src.getCorrelationSetDecls()) {
                        try {
                            compile(cset);
                        } catch (CompilationException ce) {
                            recoveredFromError(cset, ce);
                        }
                    }

                    for (PartnerLink plink : src.getPartnerLinks()) {
                        try {
                            compile(plink);
                        } catch (CompilationException ce) {
                            recoveredFromError(plink, ce);
                        }
                    }

                    if (!src.getEvents().isEmpty() || !src.getAlarms().isEmpty()) {
                        oscope.setEventHandler(new org.apache.ode.bpel.obj.OEventHandler(_oprocess));
                        oscope.getEventHandler().setDebugInfo(createDebugInfo(src, ("Event Handler for " + src)));
                    }

                    for (OnEvent onEvent : src.getEvents()) {
                        try {
                            compile(onEvent);
                        } catch (CompilationException ce) {
                            recoveredFromError(src, ce);
                        }
                    }

                    for (OnAlarm onAlarm : src.getAlarms()) {
                        try {
                            compile(onAlarm);
                        } catch (CompilationException ce) {
                            recoveredFromError(src, ce);
                        }

                    }

                    if (init != null)
                        try {
                            init.run();
                        } catch (CompilationException ce) {
                            recoveredFromError(src, ce);
                        }

                    try {
                        compile(src.getCompensationHandler());
                    } catch (CompilationException bce) {
                        recoveredFromError(src.getCompensationHandler(), bce);
                    }

                    try {
                        compile(src.getTerminationHandler());
                    } catch (CompilationException bce) {
                        recoveredFromError(src.getTerminationHandler(), bce);
                    }

                    try {
                        compile(src.getFaultHandler());
                    } catch (CompilationException bce) {
                        recoveredFromError(src.getFaultHandler(), bce);
                    }
                }
            });
        } finally {
            _atomicScope = previousAtomicScope;
        }

        return oscope;
    }

    private void compile(final OnAlarm onAlarm) {
        OScope oscope = _structureStack.topScope();
        assert oscope.getEventHandler() != null;

        final OEventHandler.OAlarm oalarm = new OEventHandler.OAlarm(_oprocess);
        oalarm.setDebugInfo(createDebugInfo(onAlarm, ("OnAlarm Event Handler: " + onAlarm)));

        if (onAlarm.getFor() != null && onAlarm.getUntil() == null) {
            oalarm.setForExpr(compileExpr(onAlarm.getFor()));
        } else if (onAlarm.getFor() == null && onAlarm.getUntil() != null) {
            oalarm.setUntilExpr(compileExpr(onAlarm.getUntil()));
        } else if (onAlarm.getFor() != null && onAlarm.getUntil() != null) {
            throw new CompilationException(__cmsgs.errInvalidAlarm().setSource(onAlarm));
        } else if (onAlarm.getRepeatEvery() == null) {
            throw new CompilationException(__cmsgs.errInvalidAlarm().setSource(onAlarm));
        }

        if (onAlarm.getRepeatEvery() != null)
            oalarm.setRepeatExpr(compileExpr(onAlarm.getRepeatEvery()));

        if (onAlarm.getActivity() == null) throw new CompilationException(__cmsgs.errInvalidAlarm().setSource(onAlarm));
        oalarm.setActivity(compile(onAlarm.getActivity()));

        // Check links crossing restrictions.
        for (OLink link : oalarm.getIncomingLinks())
            try {
                throw new CompilationException(__cmsgs.errLinkCrossesEventHandlerBoundary(link.getName()).setSource(onAlarm));
            } catch (CompilationException ce) {
                recoveredFromError(onAlarm, ce);
            }

        for (OLink link : oalarm.getOutgoingLinks())
            try {
                throw new CompilationException(__cmsgs.errLinkCrossesEventHandlerBoundary(link.getName()).setSource(onAlarm));
            } catch (CompilationException ce) {
                recoveredFromError(onAlarm, ce);
            }

        oscope.getEventHandler().getOnAlarms().add(oalarm);
    }

    private void compile(final OnEvent onEvent) {
        final OScope oscope = _structureStack.topScope();
        assert oscope.getEventHandler() != null;

        final OEventHandler.OEvent oevent = new OEventHandler.OEvent(_oprocess, oscope);
        oevent.setName("__eventHandler:");
        oevent.setDebugInfo(createDebugInfo(onEvent, null));
        compile(oevent, onEvent, new Runnable() {
            public void run() {
                switch (_processDef.getBpelVersion()) {
                case BPEL11:
                    oevent.setVariable(resolveMessageVariable(onEvent.getVariable()));
                    break;
                case BPEL20_DRAFT:
                case BPEL20:
                    if (onEvent.getMessageType() == null && onEvent.getElementType() == null)
                        throw new CompilationException(__cmsgs.errVariableDeclMissingType(onEvent.getVariable()).setSource(onEvent));
                    if (onEvent.getMessageType() != null && onEvent.getElementType() != null)
                        throw new CompilationException(__cmsgs.errVariableDeclInvalid(onEvent.getVariable()).setSource(onEvent));

                    OVarType varType;
                    if (onEvent.getMessageType() != null)
                        varType = resolveMessageType(onEvent.getMessageType());
                    else if (onEvent.getElement() != null)
                        varType = resolveElementType(onEvent.getElementType());
                    else
                        throw new CompilationException(__cmsgs.errUnrecognizedVariableDeclaration(onEvent.getVariable()));

                    oevent.setVariable(new org.apache.ode.bpel.obj.OScope.Variable(_oprocess , varType));
                    oevent.getVariable().setName(onEvent.getVariable());
                    oevent.getVariable().setDeclaringScope(_structureStack.topScope());

                    oevent.addLocalVariable(oevent.getVariable());
                    break;
                default:
                    throw new AssertionError("Unexpected BPEL VERSION constatnt: " + _processDef.getBpelVersion());
                }

                oevent.setPartnerLink(resolvePartnerLink(onEvent.getPartnerLink()));
                oevent.setOperation(resolveMyRoleOperation(oevent.getPartnerLink(), onEvent.getOperation()));
                oevent.setMessageExchangeId(onEvent.getMessageExchangeId());
                oevent.setRoute(onEvent.getRoute());

                if (onEvent.getPortType() != null && !onEvent.getPortType().equals(oevent.getPartnerLink().getMyRolePortType().getQName()))
                    throw new CompilationException(__cmsgs.errPortTypeMismatch(onEvent.getPortType(),
                            oevent.getPartnerLink().getMyRolePortType().getQName()));

                Set<String> csetNames = new HashSet<String>(); // prevents duplicate cset in on one set of correlations
                for (Correlation correlation : onEvent.getCorrelations()) {
                    if( csetNames.contains(correlation.getCorrelationSet() ) ) {
                        throw new CompilationException(__cmsgs.errDuplicateUseCorrelationSet(correlation
                                .getCorrelationSet()));
                    }

                    OScope.CorrelationSet cset = resolveCorrelationSet(correlation.getCorrelationSet());

                    switch (correlation.getInitiate()) {
                    case UNSET:
                    case NO:
                        oevent.getMatchCorrelations().add(cset);
                        oevent.getPartnerLink().addCorrelationSetForOperation(oevent.getOperation(), cset, false);
                        break;
                    case YES:
                        oevent.getInitCorrelations().add(cset);
                        break;
                    case JOIN:
                        cset.setHasJoinUseCases(true);
                        oevent.getJoinCorrelations().add(cset);
                        oevent.getPartnerLink().addCorrelationSetForOperation(oevent.getOperation(), cset, true);
                    }

                    for (OProcess.OProperty property : cset.getProperties()) {
                        // Force resolution of alias, to make sure that we have
                        // one for this variable-property pair.
                        resolvePropertyAlias(oevent.getVariable(), property.getName());
                    }

                    csetNames.add(correlation.getCorrelationSet());
                }

                if (onEvent.getActivity() == null) throw new CompilationException(__cmsgs.errInvalidAlarm().setSource(onEvent));
                oevent.setActivity(compile(onEvent.getActivity()));
            }
        });

        // Check links crossing restrictions.
        for (OLink link : oevent.getIncomingLinks())
            try {
                throw new CompilationException(__cmsgs.errLinkCrossesEventHandlerBoundary(link.getName()));
            } catch (CompilationException ce) {
                recoveredFromError(onEvent, ce);
            }

        for (OLink link : oevent.getOutgoingLinks())
            try {
                throw new CompilationException(__cmsgs.errLinkCrossesEventHandlerBoundary(link.getName()));
            } catch (CompilationException ce) {
                recoveredFromError(onEvent, ce);
            }

        oscope.getEventHandler().getOnMessages().add(oevent);
    }

    private DebugInfo createDebugInfo(BpelObject bpelObject, String description) {
        int lineNo = bpelObject == null ? -1 : bpelObject.getLineNo();
        String str = description == null && bpelObject != null ? bpelObject.toString() : null;
        Map<QName, Object> extElmt = bpelObject == null ? null : bpelObject.getExtensibilityElements();
        DebugInfo debugInfo = new DebugInfo(_processDef.getSource(), lineNo, extElmt);
        debugInfo.setDescription(str);
        return debugInfo;
    }

    private void compile(final Variable src) {
        final OScope oscope = _structureStack.topScope();

        if (src.getKind() == null)
            throw new CompilationException(__cmsgs.errVariableDeclMissingType(src.getName()).setSource(src));

        if (oscope.getLocalVariable(src.getName()) != null)
            throw new CompilationException(__cmsgs.errDuplicateVariableDecl(src.getName()).setSource(src));

        if (src.getTypeName() == null) throw new CompilationException(__cmsgs.errUnrecognizedVariableDeclaration(src.getName()));
        OVarType varType;
        switch (src.getKind()) {
        case ELEMENT:
            varType = resolveElementType(src.getTypeName());
            break;
        case MESSAGE:
            varType = resolveMessageType(src.getTypeName());
            break;
        case SCHEMA:
            varType = resolveXsdType(src.getTypeName());
            break;
        default:
            throw new CompilationException(__cmsgs.errUnrecognizedVariableDeclaration(src.getName()));
        }

        OScope.Variable ovar = new OScope.Variable(_oprocess, varType);
        ovar.setName(src.getName());
        ovar.setDeclaringScope(oscope);
        ovar.setDebugInfo(createDebugInfo(src, null));

        ovar.setExtVar(compileExtVar(src));

        oscope.addLocalVariable(ovar);

        if (__log.isDebugEnabled())
            __log.debug("Compiled variable " + ovar);
    }


    private void compile(TerminationHandler terminationHandler) {
        OScope oscope = _structureStack.topScope();
        oscope.setTerminationHandler(new org.apache.ode.bpel.obj.OTerminationHandler(_oprocess , oscope));
        oscope.getTerminationHandler().setName("__terminationHandler:" + (oscope.getName()));
        oscope.getTerminationHandler().setDebugInfo(createDebugInfo(terminationHandler, null));
        if (terminationHandler == null) {
            oscope.getTerminationHandler().setActivity(createDefaultCompensateActivity(null, ("Auto-generated \'compensate all\' pseudo-activity for default termination handler on " + (oscope.toString()))));
        } else {
            _recoveryContextStack.push(oscope);
            try {
                oscope.getTerminationHandler().setActivity(compile(terminationHandler.getActivity()));
            } finally {
                _recoveryContextStack.pop();
            }
        }
    }

    private void compile(CompensationHandler compensationHandler) {
        OScope oscope = _structureStack.topScope();
        oscope.setCompensationHandler(new org.apache.ode.bpel.obj.OCompensationHandler(_oprocess , oscope));
        oscope.getCompensationHandler().setName("__compenationHandler_" + (oscope.getName()));
        oscope.getCompensationHandler().setDebugInfo(createDebugInfo(compensationHandler, null));
        if (compensationHandler == null) {
            oscope.getCompensationHandler().setActivity(createDefaultCompensateActivity(compensationHandler, ("Auto-generated \'compensate all\' pseudo-activity for default compensation handler on  " + (oscope.toString()))));
        } else {
            _recoveryContextStack.push(oscope);
            try {
                oscope.getCompensationHandler().setActivity(compile(compensationHandler.getActivity()));
            } finally {
                _recoveryContextStack.pop();
            }
        }
    }

    private void compile(FaultHandler fh) {
        OScope oscope = _structureStack.topScope();
        oscope.setFaultHandler(new org.apache.ode.bpel.obj.OFaultHandler(_oprocess));
        if (fh == null) {
            // The default fault handler compensates all child activities
            // AND then rethrows the fault!
            final OCatch defaultCatch = new OCatch(_oprocess, oscope);
            defaultCatch.setName("__defaultFaultHandler:" + (oscope.getName()));
            defaultCatch.setFaultName(null); // catch any fault
            defaultCatch.setFaultVariable(null);
            OSequence sequence = new OSequence(_oprocess, defaultCatch);
            sequence.setName("__defaultFaultHandler_sequence:" + (oscope.getName()));
            sequence.setDebugInfo(createDebugInfo(fh, "Auto-generated sequence activity."));
            ORethrow rethrow = new ORethrow(_oprocess, sequence);
            rethrow.setName("__defaultFaultHandler_rethrow:" + (oscope.getName()));
            rethrow.setDebugInfo(createDebugInfo(fh, "Auto-generated re-throw activity."));
            sequence.getSequence().add(createDefaultCompensateActivity(fh, "Default compensation handler for " + oscope));
            sequence.getSequence().add(rethrow);

            defaultCatch.setActivity(sequence);
            oscope.getFaultHandler().getCatchBlocks().add(defaultCatch);
            if (__log.isDebugEnabled())
                __log.debug("Compiled default catch block " + defaultCatch + " for " + oscope);

        } else {
            _recoveryContextStack.push(oscope);
            try {

                int i = 0;
                for (final Catch catchSrc : fh.getCatches()) {
                    final OCatch ctch = new OCatch(_oprocess, oscope);
                    ctch.setDebugInfo(createDebugInfo(catchSrc, catchSrc.toString()));
                    ctch.setName((("__catch#" + i) + ":") + (_structureStack.topScope().getName()));
                    ctch.setFaultName(catchSrc.getFaultName());
                    compile(ctch, catchSrc, new Runnable() {
                        public void run() {

                            if (catchSrc.getFaultVariable() != null) {
                                OScope.Variable faultVar;
                                switch (_processDef.getBpelVersion()) {
                                case BPEL11:
                                    faultVar = resolveVariable(catchSrc.getFaultVariable());
                                    if (!(faultVar.getType() instanceof OMessageVarType))
                                        throw new CompilationException(__cmsgs.errMessageVariableRequired(
                                                catchSrc.getFaultVariable()).setSource(catchSrc));
                                    break;
                                case BPEL20_DRAFT:
                                case BPEL20:
                                    if (catchSrc.getFaultVariableMessageType() == null
                                            && catchSrc.getFaultVariableElementType() == null)
                                        throw new CompilationException(__cmsgs.errVariableDeclMissingType(
                                                catchSrc.getFaultVariable()).setSource(catchSrc));
                                    if (catchSrc.getFaultVariableMessageType() != null
                                            && catchSrc.getFaultVariableElementType() != null)
                                        throw new CompilationException(__cmsgs.errVariableDeclMissingType(
                                                catchSrc.getFaultVariable()).setSource(catchSrc));

                                    OVarType faultVarType;
                                    if (catchSrc.getFaultVariableMessageType() != null)
                                        faultVarType = resolveMessageType(catchSrc.getFaultVariableMessageType());
                                    else if (catchSrc.getFaultVariableElementType() != null)
                                        faultVarType = resolveElementType(catchSrc.getFaultVariableElementType());
                                    else
                                        throw new CompilationException(__cmsgs.errUnrecognizedVariableDeclaration(catchSrc
                                                .getFaultVariable()));

                                    faultVar = new OScope.Variable(_oprocess, faultVarType);
                                    faultVar.setName(catchSrc.getFaultVariable());
                                    faultVar.setDeclaringScope(_structureStack.topScope());

                                    ctch.addLocalVariable(faultVar);
                                    break;
                                default:
                                    throw new AssertionError("Unexpected BPEL VERSION constatnt: " + _processDef.getBpelVersion());
                                }

                                ctch.setFaultVariable(faultVar);
                            }

                            if (catchSrc.getActivity() == null)
                                throw new CompilationException(__cmsgs.errEmptyCatch().setSource(catchSrc));
                            _structureStack.topScope().setActivity(compile(catchSrc.getActivity()));
                        }
                    });
                    oscope.getFaultHandler().getCatchBlocks().add(ctch);
                    ++i;
                }
            } finally {
                _recoveryContextStack.pop();
            }

        }
    }

    public OXslSheet compileXslt(String docStrUri) throws CompilationException {
        URI docUri;
        try {
            docUri = new URI(FileUtils.encodePath(docStrUri));
        } catch (URISyntaxException e) {
            throw new CompilationException(__cmsgs.errInvalidDocXsltUri(docStrUri));
        }

        String sheetBody = loadXsltSheet(_processURI.resolve(docUri));
        if (sheetBody == null) {
            throw new CompilationException(__cmsgs.errCantFindXslt(docStrUri));
        }

        OXslSheet oXslSheet = new OXslSheet(_oprocess);
        oXslSheet.setUri(docUri);
        oXslSheet.setSheetBody(sheetBody);

        _oprocess.getXslSheets().put(oXslSheet.getUri(), oXslSheet);
        return oXslSheet;
    }

    private String loadXsltSheet(URI uri) {

        // TODO: lots of null returns, should have some better error messages.
        InputStream is;
        try {
            is = _resourceFinder.openResource(uri);
        } catch (Exception e1) {
            return null;
        }
        if (is == null)
            return null;

        try {
            // verify that sheet is well-formed and preserve encoding.
            Document doc = DOMUtils.parse(is);
            return DOMUtils.domToString(doc);
        } catch (Exception e) {
            __log.debug("IO error", e);
            // todo: this should produce a message
            return null;
        } finally {
            try {
                is.close();
            } catch (Exception ex) {
                // No worries.
            }
        }
    }

    public boolean isPartnerLinkAssigned(String plink) {
        for (OActivity act : _compiledActivities) {
            if (act instanceof OAssign) {
                OAssign assign = (OAssign) act;
                for (OAssign.OAssignOperation operation : assign.getOperations()) {
                    if (operation instanceof OAssign.Copy) {
                        OAssign.Copy copy = (OAssign.Copy) operation;
                        if (copy.getTo() instanceof OAssign.PartnerLinkRef) {
                            if (((OAssign.PartnerLinkRef) copy.getTo()).getPartnerLink().getName()
                                    .equals(plink))
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Registers a declared extension. Since compilation may take place independently of the target
     * engine configuration, the compiler will not check whether a extension implementation is
     * registered.
     */
    private void compileExtension(Extension ext) {
        OProcess.OExtension oextension = new OProcess.OExtension(_oprocess);
        oextension.setNamespace(ext.getNamespaceURI());
        oextension.setMustUnderstand(ext.isMustUnderstand());

        oextension.setDebugInfo(createDebugInfo(_processDef, "Extension " + ext.getNamespaceURI()));

        _declaredExtensionNS.add(ext.getNamespaceURI());
        _oprocess.getDeclaredExtensions().add(oextension);
        if (ext.isMustUnderstand()) {
            _oprocess.getMustUnderstandExtensions().add(oextension);
        }

        if (__log.isDebugEnabled())
            __log.debug("Compiled extension " + oextension);
    }

    public Definition[] getWsdlDefinitions() {
        Definition[] result = new Definition[_wsdlRegistry.getDefinitions().length];
        for (int m = 0; m < _wsdlRegistry.getDefinitions().length; m++) {
            Definition4BPEL definition4BPEL = _wsdlRegistry.getDefinitions()[m];
            result[m] = definition4BPEL.getDefinition();
        }
        return result;
    }

    private OElementVarType resolveElementType(QName faultVariableElementType) {
        OElementVarType type = _oprocess.getElementTypes().get(faultVariableElementType);
        if (type == null) {
            type = new OElementVarType(_oprocess, faultVariableElementType);
            _oprocess.getElementTypes().put(faultVariableElementType, type);
        }
        return type;
    }

    @SuppressWarnings("rawtypes")
    private ActivityGenerator findActivityGen(Activity source) {

        Class actClass = source.getClass();

        for (Map.Entry<Class, ActivityGenerator> me : _actGenerators.entrySet()) {
            Class<?> cls = me.getKey();
            if (cls.isAssignableFrom(actClass)) {
                ActivityGenerator gen = me.getValue();
                gen.setContext(this);
                return gen;
            }
        }

        throw new CompilationException(__cmsgs.errUnknownActivity(actClass.getName()).setSource(source));
    }

    @SuppressWarnings("rawtypes")
    protected void registerActivityCompiler(Class defClass, ActivityGenerator generator) {
        if (__log.isDebugEnabled()) {
            __log.debug("Adding compiler for nodes class \"" + defClass.getName() + " = " + generator);
        }

        _actGenerators.put(defClass, generator);
    }

    private ExpressionCompiler findExpLangCompiler(String expLang) {
        ExpressionCompiler compiler = _expLanguageCompilers.get(expLang);
        if (compiler == null) {
            throw new CompilationException(__cmsgs.errUnknownExpressionLanguage(expLang));
        }
        return compiler;
    }

    private String getExpressionLanguage(Expression exp) {
        String expLang = exp.getExpressionLanguage();

        if (expLang == null)
            expLang = _processDef.getExpressionLanguage();
        if (expLang == null)
            expLang = getDefaultExpressionLanguage();
        return expLang;
    }

    protected abstract String getDefaultExpressionLanguage();

    protected abstract String getBpwsNamespace();

    protected void registerExpressionLanguage(String expLangUri, ExpressionCompiler expressionCompiler) {
        _expLanguageCompilers.put(expLangUri, expressionCompiler);
    }

    @SuppressWarnings("rawtypes")
    protected void registerExpressionLanguage(String expLangUri, String classname) throws Exception {
        Class cls = Class.forName(classname);
        registerExpressionLanguage(expLangUri, (ExpressionCompiler) cls.newInstance());
    }

    public boolean isExtensionDeclared(String namespace) {
        return _declaredExtensionNS.contains(namespace);
    }

    public List<OActivity> getActivityStack() {
        ArrayList<OActivity> rval = new ArrayList<OActivity>(_structureStack._stack);
        Collections.reverse(rval);
        return rval;
    }

    public Map<URI, Source> getSchemaSources() {
        return _wsdlRegistry.getSchemaSources();
    }

    /**
     * Retrieves the base URI that the BPEL Process execution contextis running relative to.
     *
     * @return URI - the URI representing the absolute physical file path location that this process is defined within.
     * @throws IOException
     * @throws MalformedURLException
     */
     public URI getBaseResourceURI() {
        return _resourceFinder.getBaseResourceURI();
    }

    public NSContext tryCacheNamespaceContext(NSContext nsContext) {
        if (getOProcess().getNamespaceContext().equals(nsContext)) {
            return getOProcess().getNamespaceContext();
        } else {
            return nsContext;
        }
    }

    /**
     * Compile external variable declaration.
     * @param src variable object
     * @return compiled {@link OExtVar} representation.
     */
    private OExtVar compileExtVar(Variable src) {
        if (!src.isExternal())
            return null;

        OExtVar oextvar = new OExtVar(_oprocess);
        oextvar.setExternalVariableId(src.getExternalId());
        oextvar.setDebugInfo(createDebugInfo(src, null));

        if (src.getExternalId() == null)
            throw new CompilationException(__cmsgs.errMustSpecifyExternalVariableId(src.getName()));

        if (src.getRelated() == null)
            throw new CompilationException(__cmsgs.errMustSpecifyRelatedVariable(src.getName()));
        oextvar.setRelated(resolveVariable(src.getRelated()));

        return oextvar;
    }

    private static class StructureStack {
        private Stack<OActivity> _stack = new Stack<OActivity>();
        private Map<OActivity,BpelObject> _srcMap = new HashMap<OActivity,BpelObject>();

        public void push(OActivity act, BpelObject src) {
            _stack.push(act);
            _srcMap.put(act, src);
        }

        public BpelObject topSource() {
            return _srcMap.get(topActivity());
        }

        public OScope topScope() {
            List<OScope> scopeStack = scopeStack();
            return scopeStack.isEmpty() ? null : scopeStack.get(scopeStack.size() - 1);
        }

        public OScope rootScope() {
            for (OActivity oActivity : _stack)
                if (oActivity instanceof OScope)
                    return (OScope) oActivity;
            return null;
        }

        public OActivity pop() {
            return _stack.pop();
        }

        public void clear() {
            _stack.clear();
        }

        public int size() {
            return _stack.size();
        }

        public Iterator<OScope> oscopeIterator() {
            List<OScope> scopeStack = scopeStack();
            Collections.reverse(scopeStack);
            return scopeStack.iterator();
        }

        private List<OScope> scopeStack() {
            ArrayList<OScope> newList = new ArrayList<OScope>();
            CollectionsX.filter(newList, _stack.iterator(), OScope.class);
            return newList;
        }

        public OActivity topActivity() {
            return _stack.isEmpty() ? null : _stack.peek();
        }

        public Iterator<OActivity> iterator() {
            ArrayList<OActivity> rval = new ArrayList<OActivity>(_stack);
            Collections.reverse(rval);
            return rval.iterator();
        }
    }
}
