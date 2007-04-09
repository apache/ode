package org.apache.ode.bpel.compiler.bom;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

/**
 * Assignment L/R-value defined in terms of message variable extensions. This is a 
 * BPEL hack (not standard BPEL) that allows the process to access custom message 
 * "extensions", for example SOAP headers and the like. Evil, use sparingly. 
 * 
 * @author Maciej Szefler ( m s z e f l e r @ g m a i l . c o m )
 */
public class ExtensionVal extends ToFrom {

    public ExtensionVal(Element el) {
        super(el);
    }

    public String getVariable() {
        return getAttribute("variable", null);
    }

    public QName getExtension() {
        return getNamespaceContext().derefQName(getAttribute("extension", null));
    }
}

