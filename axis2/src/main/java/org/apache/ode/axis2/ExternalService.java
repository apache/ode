package org.apache.ode.axis2;

import org.apache.ode.bpel.iapi.PartnerRoleChannel;
import org.apache.ode.bpel.iapi.PartnerRoleMessageExchange;
import javax.xml.namespace.QName;

/**
 +  * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public interface ExternalService extends PartnerRoleChannel {
    void invoke(PartnerRoleMessageExchange odeMex);

    String getPortName();

    QName getServiceName();
}
