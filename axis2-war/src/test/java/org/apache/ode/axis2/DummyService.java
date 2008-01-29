package org.apache.ode.axis2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class DummyService {
    public String hello(String in) {
        return in + " world";
    }

    public String faultTest(String in) throws DummyException, AxisFault {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement(new QName("http://axis2.ode.apache.org", "DummyException"));
        OMElement reason = factory.createOMElement(new QName("http://axis2.ode.apache.org", "reason"));
        reason.setText("Something went wrong. Fortunately, it was meant to be.");
        root.addChild(reason);
        throw new AxisFault(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"), "dummy reason",
                "dummy node", "dummy role", root);
    }
}
