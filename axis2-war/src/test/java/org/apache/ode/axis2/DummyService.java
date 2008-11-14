package org.apache.ode.axis2;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class DummyService {

    private static final Log log = LogFactory.getLog(DummyService.class);

    public String hello(String in) {
        log.debug("#### IN HELLO ####");
        return in + " world";
    }

    public String longOperation(String in) {
        long delay = 120000; // == Properties.DEFAULT_MEX_TIMEOUT
        try {
            delay = Long.parseLong(in);
        } catch (NumberFormatException ignore) {}
        try {
            log.debug("#### IN LONG OP: "+delay+"ms ####");
            Thread.sleep(delay);
        } catch (InterruptedException ignore) { }
        log.debug("#### WENT THROUGH ###");
        return "Went through " + in;
    }

    public String faultTest(String in) throws DummyException, AxisFault {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement root = factory.createOMElement(new QName("http://axis2.ode.apache.org", "DummyException"));
        OMElement reason = factory.createOMElement(new QName("", "reason"));
        reason.setText("Something went wrong. Fortunately, it was meant to be.");
        root.addChild(reason);
        throw new AxisFault(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Client"), "dummy reason",
                "dummy node", "dummy role", root);
    }
}
