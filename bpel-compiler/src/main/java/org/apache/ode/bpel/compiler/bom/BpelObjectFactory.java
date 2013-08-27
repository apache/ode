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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.compiler.bom.IfActivity.Case;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.XMLParserUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BpelObjectFactory {

    private static final Log __log = LogFactory.getLog(BpelObjectFactory.class);
    private static BpelObjectFactory __instance = new BpelObjectFactory();
    
    public static final String WSDL = "http://schemas.xmlsoap.org/wsdl/";
    public static final String XML = "http://www.w3.org/2001/xml.xsd";
    
    private final Map<QName, Class<? extends BpelObject>> _mappings = new HashMap<QName, Class<? extends BpelObject>>();

    private Class[] __CTOR = { Element.class };

    public BpelObjectFactory() {
        
        //
        // BPEL 2.0 Final Mappings
        //
        _mappings.put(Bpel20QNames.FINAL_PROCESS, Process.class);
        _mappings.put(Bpel20QNames.FINAL_ASSIGN, AssignActivity.class);
        _mappings.put(Bpel20QNames.FINAL_EMPTY, EmptyActivity.class);
        _mappings.put(Bpel20QNames.FINAL_INVOKE, InvokeActivity.class);
        _mappings.put(Bpel20QNames.FINAL_RECEIVE, ReceiveActivity.class);
        _mappings.put(Bpel20QNames.FINAL_REPLY, ReplyActivity.class);
        _mappings.put(Bpel20QNames.FINAL_WAIT, WaitActivity.class);
        _mappings.put(Bpel20QNames.FINAL_THROW, ThrowActivity.class);
        _mappings.put(Bpel20QNames.FINAL_COMPENSATE, CompensateActivity.class);
        _mappings.put(Bpel20QNames.FINAL_COMPENSATE_SCOPE, CompensateScopeActivity.class);
        _mappings.put(Bpel20QNames.FINAL_RETHROW, RethrowActivity.class);
        _mappings.put(Bpel20QNames.FINAL_EXIT, TerminateActivity.class);
        _mappings.put(Bpel20QNames.FINAL_FLOW, FlowActivity.class);
        _mappings.put(Bpel20QNames.FINAL_SWITCH, IfActivity.class);
        _mappings.put(Bpel20QNames.FINAL_IF, IfActivity.class);
        _mappings.put(Bpel20QNames.FINAL_WHILE, WhileActivity.class);
        _mappings.put(Bpel20QNames.FINAL_REPEATUNTIL,RepeatUntilActivity.class);
        _mappings.put(Bpel20QNames.FINAL_SEQUENCE, SequenceActivity.class);
        _mappings.put(Bpel20QNames.FINAL_PICK, PickActivity.class);
        _mappings.put(Bpel20QNames.FINAL_SCOPE, ScopeActivity.class);
        _mappings.put(Bpel20QNames.FINAL_FOREACH, ForEachActivity.class);
        _mappings.put(Bpel20QNames.FINAL_COMPLETION_CONDITION, CompletionCondition.class);
        _mappings.put(Bpel20QNames.FINAL_BRANCHES, Branches.class);
        _mappings.put(Bpel20QNames.FINAL_COPY, Copy.class);
        _mappings.put(Bpel20QNames.FINAL_CATCH, Catch.class);
        _mappings.put(Bpel20QNames.FINAL_CATCHALL, Catch.class);
        _mappings.put(Bpel20QNames.FINAL_TO, To.class);
        _mappings.put(Bpel20QNames.FINAL_FROM, From.class);
        _mappings.put(Bpel20QNames.FINAL_START_COUNTER_VALUE, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_FINAL_COUNTER_VALUE, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_CORRELATION, Correlation.class);
        _mappings.put(Bpel20QNames.FINAL_CORRELATIONSET, CorrelationSet.class);
        _mappings.put(Bpel20QNames.FINAL_COMPENSATE, CompensateActivity.class);
        _mappings.put(Bpel20QNames.FINAL_COMPENSATE_SCOPE, CompensateScopeActivity.class);
        _mappings.put(Bpel20QNames.FINAL_COMPENSATIONHANDLER, CompensationHandler.class);
        _mappings.put(Bpel20QNames.FINAL_FAULTHANDLERS, FaultHandler.class);
        _mappings.put(Bpel20QNames.FINAL_TERMINATIONHANDLER, TerminationHandler.class);
        _mappings.put(Bpel20QNames.FINAL_CASE, IfActivity.Case.class);
        _mappings.put(Bpel20QNames.FINAL_CONDITION, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_UNTIL, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_FOR, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_REPEAT_EVERY, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_ONALARM, OnAlarm.class);
        _mappings.put(Bpel20QNames.FINAL_ONEVENT, OnEvent.class);
        _mappings.put(Bpel20QNames.FINAL_ONMESSAGE, OnMessage.class);
        _mappings.put(Bpel20QNames.FINAL_LITERAL, LiteralVal.class);
        _mappings.put(Bpel20QNames.FINAL_PLINKTYPE, PartnerLinkType.class);
        _mappings.put(Bpel20QNames.FINAL_PLINKROLE, PartnerLinkType.Role.class);
        _mappings.put(Bpel20QNames.FINAL_PROPALIAS, PropertyAlias.class);
        _mappings.put(Bpel20QNames.FINAL_PROPQUERY, Query.class);
        _mappings.put(Bpel20QNames.FINAL_PROPERTY,  Property.class);
        _mappings.put(Bpel20QNames.FINAL_VARIABLES, Variables.class);
        _mappings.put(Bpel20QNames.FINAL_VARIABLE, Variable.class);
        _mappings.put(Bpel20QNames.FINAL_PARTNERLINKS, PartnerLinks.class);
        _mappings.put(Bpel20QNames.FINAL_PARTNERLINK, PartnerLink.class);
        _mappings.put(Bpel20QNames.FINAL_CORRELATIONSETS, CorrelationSets.class);
        _mappings.put(Bpel20QNames.FINAL_JOIN_CONDITION, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_LINKS, Links.class );
        _mappings.put(Bpel20QNames.FINAL_LINK, Link.class);
        _mappings.put(Bpel20QNames.FINAL_SOURCE, LinkSource.class);
        _mappings.put(Bpel20QNames.FINAL_TARGET, LinkTarget.class);
        _mappings.put(Bpel20QNames.FINAL_SOURCES, BpelObject.class);
        _mappings.put(Bpel20QNames.FINAL_TARGETS, BpelObject.class);
        _mappings.put(Bpel20QNames.FINAL_IMPORT, Import.class);
        _mappings.put(Bpel20QNames.FINAL_QUERY, Query.class);
        _mappings.put(Bpel20QNames.FINAL_TRANSITION_CONDITION, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_THEN, Case.class);
        _mappings.put(Bpel20QNames.FINAL_ELSE, Case.class);
        _mappings.put(Bpel20QNames.FINAL_ELSEIF, Case.class);
        _mappings.put(Bpel20QNames.FINAL_CORRELATIONS, Correlations.class);
        _mappings.put(Bpel20QNames.FINAL_EVENTHANDLERS, BpelObject.class);
        _mappings.put(Bpel20QNames.FINAL_TARGETS,Targets.class);
        _mappings.put(Bpel20QNames.FINAL_SOURCES,Sources.class);

        //
        // BPEL 2.0 draft Mappings
        //
        _mappings.put(Bpel20QNames.PROCESS, Process.class);
        _mappings.put(Bpel20QNames.ASSIGN, AssignActivity.class);
        _mappings.put(Bpel20QNames.EMPTY, EmptyActivity.class);
        _mappings.put(Bpel20QNames.INVOKE, InvokeActivity.class);
        _mappings.put(Bpel20QNames.RECEIVE, ReceiveActivity.class);
        _mappings.put(Bpel20QNames.REPLY, ReplyActivity.class);
        _mappings.put(Bpel20QNames.WAIT, WaitActivity.class);
        _mappings.put(Bpel20QNames.THROW, ThrowActivity.class);
        _mappings.put(Bpel20QNames.COMPENSATE, CompensateScopeActivity.class);
        _mappings.put(Bpel20QNames.RETHROW, RethrowActivity.class);
        _mappings.put(Bpel20QNames.EXIT, TerminateActivity.class);
        _mappings.put(Bpel20QNames.FLOW, FlowActivity.class);
        _mappings.put(Bpel20QNames.SWITCH, IfActivity.class);
        _mappings.put(Bpel20QNames.IF, IfActivity.class);
        _mappings.put(Bpel20QNames.WHILE, WhileActivity.class);
        _mappings.put(Bpel20QNames.REPEATUNTIL,RepeatUntilActivity.class);
        _mappings.put(Bpel20QNames.SEQUENCE, SequenceActivity.class);
        _mappings.put(Bpel20QNames.PICK, PickActivity.class);
        _mappings.put(Bpel20QNames.SCOPE, ScopeActivity.class);
        _mappings.put(Bpel20QNames.FOREACH, ForEachActivity.class);
        _mappings.put(Bpel20QNames.COPY, Copy.class);
        _mappings.put(Bpel20QNames.CATCH, Catch.class);
        _mappings.put(Bpel20QNames.CATCHALL, Catch.class);
        _mappings.put(Bpel20QNames.TO, To.class);
        _mappings.put(Bpel20QNames.FROM, From.class);
        _mappings.put(Bpel20QNames.START_COUNTER_VALUE, Expression.class);
        _mappings.put(Bpel20QNames.FINAL_COUNTER_VALUE, Expression.class);
        _mappings.put(Bpel20QNames.COMPLETION_CONDITION, CompletionCondition.class);
        _mappings.put(Bpel20QNames.BRANCHES, Branches.class);
        _mappings.put(Bpel20QNames.CORRELATION, Correlation.class);
        _mappings.put(Bpel20QNames.CORRELATIONSET, CorrelationSet.class);
        _mappings.put(Bpel20QNames.COMPENSATE, CompensateScopeActivity.class);
        _mappings.put(Bpel20QNames.COMPENSATIONHANDLER, CompensationHandler.class);
        _mappings.put(Bpel20QNames.FAULTHANDLERS, FaultHandler.class);
        _mappings.put(Bpel20QNames.TERMINATIONHANDLER, TerminationHandler.class);
        _mappings.put(Bpel20QNames.CASE, IfActivity.Case.class);
        _mappings.put(Bpel20QNames.CONDITION, Expression.class);
        _mappings.put(Bpel20QNames.UNTIL, Expression.class);
        _mappings.put(Bpel20QNames.FOR, Expression.class);
        _mappings.put(Bpel20QNames.REPEAT_EVERY, Expression.class);
        _mappings.put(Bpel20QNames.ONALARM, OnAlarm.class);
        _mappings.put(Bpel20QNames.ONEVENT, OnEvent.class);
        _mappings.put(Bpel20QNames.ONMESSAGE, OnMessage.class);
        _mappings.put(Bpel20QNames.LITERAL, LiteralVal.class);
        _mappings.put(Bpel20QNames.PLINKTYPE, PartnerLinkType.class);
        _mappings.put(Bpel20QNames.PLINKROLE, PartnerLinkType.Role.class);
        _mappings.put(Bpel20QNames.PROPALIAS, PropertyAlias.class);
        _mappings.put(Bpel20QNames.PROPERTY,  Property.class);
        _mappings.put(Bpel20QNames.VARIABLES, Variables.class);
        _mappings.put(Bpel20QNames.VARIABLE, Variable.class);
        _mappings.put(Bpel20QNames.PARTNERLINKS, PartnerLinks.class);
        _mappings.put(Bpel20QNames.PARTNERLINK, PartnerLink.class);
        _mappings.put(Bpel20QNames.CORRELATIONSETS, CorrelationSets.class);
        _mappings.put(Bpel20QNames.JOIN_CONDITION, Expression.class);
        _mappings.put(Bpel20QNames.LINKS, Links.class );
        _mappings.put(Bpel20QNames.LINK, Link.class);
        _mappings.put(Bpel20QNames.SOURCE, LinkSource.class);
        _mappings.put(Bpel20QNames.TARGET, LinkTarget.class);
        _mappings.put(Bpel20QNames.SOURCES, BpelObject.class);
        _mappings.put(Bpel20QNames.TARGETS, BpelObject.class);
        _mappings.put(Bpel20QNames.IMPORT, Import.class);
        _mappings.put(Bpel20QNames.QUERY, Query.class);
        _mappings.put(Bpel20QNames.TRANSITION_CONDITION, Expression.class);
        _mappings.put(Bpel20QNames.THEN, Case.class);
        _mappings.put(Bpel20QNames.ELSE, Case.class);
        _mappings.put(Bpel20QNames.ELSEIF, Case.class);
        _mappings.put(Bpel20QNames.CORRELATIONS, Correlations.class);
        _mappings.put(Bpel20QNames.EVENTHANDLERS, BpelObject.class);
        _mappings.put(Bpel20QNames.TARGETS,Targets.class);
        _mappings.put(Bpel20QNames.SOURCES,Sources.class);
        _mappings.put(Bpel20QNames.RDF_LABEL,RdfLabel.class);

        //
        // BPEL 1.1 Mappings
        //
        _mappings.put(Bpel11QNames.PROCESS, Process.class);
        _mappings.put(Bpel11QNames.ASSIGN, AssignActivity.class);
        _mappings.put(Bpel11QNames.EMPTY, EmptyActivity.class);
        _mappings.put(Bpel11QNames.INVOKE, InvokeActivity.class);
        _mappings.put(Bpel11QNames.RECEIVE, ReceiveActivity.class);
        _mappings.put(Bpel11QNames.REPLY, ReplyActivity.class);
        _mappings.put(Bpel11QNames.WAIT, WaitActivity.class);
        _mappings.put(Bpel11QNames.THROW, ThrowActivity.class);
        _mappings.put(Bpel11QNames.TERMINATE, TerminateActivity.class);
        _mappings.put(Bpel11QNames.FLOW, FlowActivity.class);
        _mappings.put(Bpel11QNames.SWITCH, SwitchActivity.class);
        _mappings.put(Bpel11QNames.WHILE, WhileActivity11.class);
        _mappings.put(Bpel11QNames.SEQUENCE, SequenceActivity.class);
        _mappings.put(Bpel11QNames.PICK, PickActivity.class);
        _mappings.put(Bpel11QNames.SCOPE, ScopeActivity.class);
        _mappings.put(Bpel11QNames.COPY, Copy.class);
        _mappings.put(Bpel11QNames.CATCH, Catch.class);
        _mappings.put(Bpel11QNames.CATCHALL, Catch.class);
        _mappings.put(Bpel11QNames.TO, To.class);
        _mappings.put(Bpel11QNames.FROM, From.class);
        _mappings.put(Bpel11QNames.CORRELATION, Correlation.class);
        _mappings.put(Bpel11QNames.CORRELATIONSET, CorrelationSet.class);
        _mappings.put(Bpel11QNames.COMPENSATE, CompensateScopeActivity.class);
        _mappings.put(Bpel11QNames.COMPENSATIONHANDLER, CompensationHandler.class);
        _mappings.put(Bpel11QNames.FAULTHANDLERS, FaultHandler.class);
        _mappings.put(Bpel11QNames.CASE, SwitchActivity.Case.class);
        _mappings.put(Bpel11QNames.OTHERWISE, SwitchActivity.Case.class);
        _mappings.put(Bpel11QNames.ONALARM, OnAlarm.class);
        _mappings.put(Bpel11QNames.ONMESSAGE, OnMessage.class);
        _mappings.put(Bpel11QNames.PLINKTYPE, PartnerLinkType.class);
        _mappings.put(Bpel11QNames.PLINKROLE, PartnerLinkType.Role11.class);
        _mappings.put(Bpel11QNames.PORTTYPE, PartnerLinkType.Role11.PortType11.class);
        _mappings.put(Bpel11QNames.PROPALIAS, PropertyAlias11.class);
        _mappings.put(Bpel11QNames.PROPERTY,  Property.class);
        _mappings.put(Bpel11QNames.VARIABLES, Variables.class);
        _mappings.put(Bpel11QNames.VARIABLE, Variable.class);
        _mappings.put(Bpel11QNames.PARTNERLINKS, PartnerLinks.class);
        _mappings.put(Bpel11QNames.PARTNERLINK, PartnerLink.class);
        _mappings.put(Bpel11QNames.CORRELATIONSETS, CorrelationSets.class);
        _mappings.put(Bpel11QNames.LINKS, Links.class );
        _mappings.put(Bpel11QNames.LINK, Link.class);
        _mappings.put(Bpel11QNames.SOURCE, LinkSource.class);
        _mappings.put(Bpel11QNames.TARGET, LinkTarget.class);
        _mappings.put(Bpel11QNames.QUERY, Query.class);
        _mappings.put(Bpel11QNames.CORRELATIONS, Correlations.class);
        _mappings.put(Bpel11QNames.EVENTHANDLERS, BpelObject.class);
        
        //
        // Extensibility Elements Mappings
        //
        _mappings.put(ExtensibilityQNames.FAILURE_HANDLING, FailureHandling.class);
        _mappings.put(ExtensibilityQNames.FAILURE_HANDLING_RETRY_FOR, FailureHandling.RetryFor.class);
        _mappings.put(ExtensibilityQNames.FAILURE_HANDLING_RETRY_DELAY, FailureHandling.RetryDelay.class);
        _mappings.put(ExtensibilityQNames.FAILURE_HANDLING_FAULT_ON, FailureHandling.FaultOnFailure.class);
        
    }

    public static BpelObjectFactory getInstance() {
        return __instance;
    }

    public BpelObject createBpelObject(Element el, URI uri) {
        QName type = new QName(el.getNamespaceURI(), el.getLocalName());
        Class cls = _mappings.get(type);
        if (cls == null) {
            __log.warn("Unrecognized element in BPEL dom: " + type);
            return new BpelObject(el);
        }
        try {
            Constructor ctor = cls.getConstructor(__CTOR);
            BpelObject bo =(BpelObject) ctor.newInstance(new Object[]{el});
            bo.setURI(uri);
            return bo;
        } catch (Exception ex) {
            throw new RuntimeException("Internal compiler error", ex); 
        }
    }

    /**
     * Parse a BPEL process found at the input source.
     * @param isrc input source.
     * @return
     * @throws SAXException 
     */
    public Process parse(InputSource isrc, URI systemURI) throws IOException, SAXException {
        XMLReader _xr = XMLParserUtils.getXMLReader();
        LocalEntityResolver resolver = new LocalEntityResolver();
        resolver.register(Bpel11QNames.NS_BPEL4WS_2003_03, getClass().getResource("/bpel4ws_1_1-fivesight.xsd"));
        resolver.register(Bpel20QNames.NS_WSBPEL2_0, getClass().getResource("/wsbpel_main-draft-Apr-29-2006.xsd"));
        resolver.register(Bpel20QNames.NS_WSBPEL2_0_FINAL_ABSTRACT, getClass().getResource("/ws-bpel_abstract_common_base.xsd"));
        resolver.register(Bpel20QNames.NS_WSBPEL2_0_FINAL_EXEC, getClass().getResource("/ws-bpel_executable.xsd"));
        resolver.register(Bpel20QNames.NS_WSBPEL2_0_FINAL_PLINK, getClass().getResource("/ws-bpel_plnktype.xsd"));
        resolver.register(Bpel20QNames.NS_WSBPEL2_0_FINAL_SERVREF, getClass().getResource("/ws-bpel_serviceref.xsd"));
        resolver.register(Bpel20QNames.NS_WSBPEL2_0_FINAL_VARPROP, getClass().getResource("/ws-bpel_varprop.xsd"));
        resolver.register(XML, getClass().getResource("/xml.xsd"));
        resolver.register(WSDL,getClass().getResource("/wsdl.xsd"));
        resolver.register(Bpel20QNames.NS_WSBPEL_PARTNERLINK_2004_03, 
                getClass().getResource("/wsbpel_plinkType-draft-Apr-29-2006.xsd"));
        _xr.setEntityResolver(resolver);
        Document doc = DOMUtils.newDocument();
        _xr.setContentHandler(new DOMBuilderContentHandler(doc));
        _xr.setFeature("http://xml.org/sax/features/namespaces",true);
        _xr.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        _xr.parse(isrc);
        return (Process) createBpelObject(doc.getDocumentElement(), systemURI);
    }
   
}
