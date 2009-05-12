package org.apache.ode.bpel.iapi;

import javax.xml.namespace.QName;
import javax.wsdl.Operation;
import javax.wsdl.PortType;

/**
 * Message exchange used for a web-service based interaction between the integration layer and the
 * engine. Adds portType and operation information.
 */
public interface WSMessageExchange extends MessageExchange {

    static String PROPERTY_SEP_MYROLE_SESSIONID = "org.apache.ode.bpel.myRoleSessionId";
    static String PROPERTY_SEP_PARTNERROLE_SESSIONID = "org.apache.ode.bpel.partnerRoleSessionId";
    static String PROPERTY_SEP_PARTNERROLE_EPR = "org.apache.ode.bpel.partnerRoleEPR";

    /**
     * Get the name of the operation (WSDL 1.1) / message exchange (WSDL 1.2?).
     *
     * @return name of the operation (WSDL 1.1) /message exchange (WSDL 1.2?).
     */
    String getOperationName() throws BpelEngineException;


    /**
     * Get a reference to the end-point targeted by this message exchange.
     * @return end-point reference for this message exchange
     */
    EndpointReference getEndpointReference() throws BpelEngineException;

    /**
     * Get the fault type.
     * @return fault type, or <code>null</code> if not available/applicable.
     */
    QName getFault();

    String getFaultExplanation();

    /**
     * Get the fault resposne message.
     * @return fault response, or <code>null</code> if not available/applicable.
     */
    Message getFaultResponse();

    /**
     * Get the operation description for this message exchange.
     * It is possible that the description cannot be resolved, for example if
     * the EPR is unknown or if the operation does not exist.
     * @return WSDL operation description or <code>null</code> if not availble
     */
    Operation getOperation();

    /**
     * Get the port type description for this message exchange.
     * It is possible that the description cannot be resolved, for example if
     * the EPR is unknown or if the operation does not exist.
     * @return WSDL port type description or <code>null</code> if not available.
     */
    PortType getPortType();

    /**
     * Report whether the operation is "safe" in the sense of the WSDL1.2 meaning of the term. That is,
     * is the operation side-effect free?
     * @return <code>true</code> if the operation is safe, <code>false</code> otherwise.
     */
    public boolean isSafe();

}
