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

import javax.xml.namespace.QName;

public abstract class Bpel20QNames {

    /**
     * The XML namespace for schema of WS-BPEL 2.0, i.e., the first
     * OASIS-sanctioned version of the specification.
     */
    public static final String NS_WSBPEL2_0 = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";

    public static final String NS_WSBPEL_PARTNERLINK_2004_03 = "http://schemas.xmlsoap.org/ws/2004/03/partner-link/";

    public static final QName PROCESS = newQName("process");

    public static final QName IMPORT = newQName("import");

    public static final QName SERVICE_REF = newQName("service-ref");

    public static final QName SOURCES = newQName("sources");

    public static final QName SOURCE = newQName("source");

    public static final QName TARGETS = newQName("targets");

    public static final QName TARGET = newQName("target");

    public static final QName PARTNERLINKS = newQName("partnerLinks");

    public static final QName PARTNERLINK = newQName("partnerLink");

    public static final QName VARIABLES = newQName("variables");

    public static final QName VARIABLE = newQName("variable");

    public static final QName CORRELATIONSETS = newQName("correlationSets");

    public static final QName CORRELATIONSET = newQName("correlationSet");

    public static final QName FAULTHANDLERS = newQName("faultHandlers");

    public static final QName CATCH = newQName("catch");

    public static final QName CATCHALL = newQName("catchAll");

    public static final QName COMPENSATIONHANDLER = newQName("compensationHandler");

    public static final QName TERMINATIONHANDLER = newQName("terminationHandler");

    public static final QName COMPENSATE = newQName("compensate");

    public static final QName EVENTHANDLERS = newQName("eventHandlers");

    public static final QName ONEVENT = newQName("onEvent");

    public static final QName ONMESSAGE = newQName("onMessage");

    public static final QName ONALARM = newQName("onAlarm");

    public static final QName CORRELATIONS = newQName("correlations");

    public static final QName CORRELATION = newQName("correlation");

    public static final QName EMPTY = newQName("empty");

    public static final QName INVOKE = newQName("invoke");

    public static final QName RECEIVE = newQName("receive");

    public static final QName REPLY = newQName("reply");

    public static final QName ASSIGN = newQName("assign");

    public static final QName COPY = newQName("copy");

    public static final QName FROM = newQName("from");

    public static final QName TO = newQName("to");

    public static final QName WAIT = newQName("wait");

    public static final QName THROW = newQName("throw");

    public static final QName RETHROW = newQName("rethrow");

    public static final QName EXIT = newQName("exit");

    public static final QName FLOW = newQName("flow");

    public static final QName LINKS = newQName("links");

    public static final QName LINK = newQName("link");

    public static final QName SWITCH = newQName("switch");

    public static final QName CASE = newQName("case");

    public static final QName OTHERWISE = newQName("otherwise");

    public static final QName IF = newQName("if");

    public static final QName THEN = newQName("then");

    public static final QName ELSEIF = newQName("elseif");

    public static final QName ELSE = newQName("else");

    public static final QName WHILE = newQName("while");

    public static final QName SEQUENCE = newQName("sequence");

    public static final QName PICK = newQName("pick");

    public static final QName SCOPE = newQName("scope");

    public static final QName TRANSITION_CONDITION = newQName("transitionCondition");

    public static final QName FOR = newQName("for");

    public static final QName UNTIL = newQName("until");

    public static final QName REPEAT_EVERY = newQName("repeatEvery");

    public static final QName JOIN_CONDITION = newQName("joinCondition");

    public static final QName CONDITION = newQName("condition");

    public static final QName QUERY = newQName("query");

    public static final QName EXPRESSION = newQName("expression");

    public static final QName FOREACH = newQName("forEach");

    public static final QName ITERATOR = newQName("iterator");

    public static final QName START_COUNTER_VALUE = newQName("startCounterValue");

    public static final QName FINAL_COUNTER_VALUE = newQName("finalCounterValue");

    public static final QName COMPLETION_CONDITION = newQName("completionCondition");

    public static final QName BRANCHES = newQName("branches");

    public static final QName LITERAL = newQName("literal");

    public static final QName PROPALIAS = newQName("propertyAlias");

    public static final QName PROPERTY = newQName("property");

    public static final QName PLINKTYPE = new QName(NS_WSBPEL_PARTNERLINK_2004_03, "partnerLinkType");

    public static final QName PLINKROLE = new QName(NS_WSBPEL_PARTNERLINK_2004_03, "role");

    private static QName newQName(String localname) {
        return new QName(NS_WSBPEL2_0, localname);
    }


}
