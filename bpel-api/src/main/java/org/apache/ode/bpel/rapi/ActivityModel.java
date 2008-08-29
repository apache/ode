package org.apache.ode.bpel.rapi;

import javax.xml.namespace.QName;
import java.util.HashMap;

public interface ActivityModel {

    HashMap<QName, Object> getExtensibilityElements();
}
