package org.apache.ode.bpel.iapi;

import javax.xml.namespace.QName;

/**
 * Concrete representation of service endpoint. This consists of a service
 * qualified name and port name per WSDL specification.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 * 
 */
public class Endpoint {
    /** Service QName */
    public final QName serviceName;

    /** Port Name */
    public final String portName;

    /** Constructor. */
    public Endpoint(QName serviceName, String portName) {
        if (serviceName == null || portName == null)
            throw new NullPointerException("serviceName and portName must not be null");
        this.serviceName = serviceName;
        this.portName = portName;
    }

    /**
     * Equality operator, check whether service name and port name are both equal.
     */
    @Override 
    public boolean equals(Object other) {
        Endpoint o = (Endpoint) other;
        return o.serviceName.equals(serviceName) && o.portName.equals(portName);
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode() ^ portName.hashCode();
    }
    
    /**
     * Print object in the form <em>serviceQName</em>:<em>port</em>
     */
    @Override
    public String toString() {
        return serviceName + ":" + portName;
    }

}
