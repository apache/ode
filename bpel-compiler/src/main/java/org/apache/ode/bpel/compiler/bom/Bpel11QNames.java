package org.apache.ode.bpel.compiler.bom;

import javax.xml.namespace.QName;


public abstract class Bpel11QNames {
    /**
     * BPEL Namespace, 03/2003
     */
    public static final String NS_BPEL4WS_2003_03 = "http://schemas.xmlsoap.org/ws/2003/03/business-process/";
    
    /**
     * BPEL Partnerlink Namespace, 05/2003
     */
    public static final String NS_BPEL4WS_PARTNERLINK_2003_05 = "http://schemas.xmlsoap.org/ws/2003/05/partner-link/";
    
    public static final QName PROCESS = newQName("process");

    public static final QName SOURCE = newQName("source");

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

    public static final QName COMPENSATE = newQName("compensate");

    public static final QName EVENTHANDLERS = newQName("eventHandlers");

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

    public static final QName TERMINATE = newQName("terminate");

    public static final QName FLOW = newQName("flow");

    public static final QName LINKS = newQName("links");

    public static final QName LINK = newQName("link");

    public static final QName SWITCH = newQName("switch");

    public static final QName CASE = newQName("case");

    public static final QName OTHERWISE = newQName("otherwise");

    public static final QName WHILE = newQName("while");

    public static final QName SEQUENCE = newQName("sequence");

    public static final QName PICK = newQName("pick");

    public static final QName SCOPE = newQName("scope");

    public static final QName QUERY = newQName("query");

    public static final QName EXPRESSION = newQName("expression");

    public static final QName PROPALIAS = newQName("propertyAlias");

    public static final QName PROPERTY = newQName("property");

    public static final QName PLINKTYPE = new QName(NS_BPEL4WS_PARTNERLINK_2003_05, "partnerLinkType");

    public static final QName PLINKROLE = new QName(NS_BPEL4WS_PARTNERLINK_2003_05, "role");


    private static QName newQName(String local) {
        return new QName(NS_BPEL4WS_2003_03, local);
    }

}
