package org.apache.ode.bpel.rapi;

import javax.xml.namespace.QName;
import java.util.Set;

public interface CorrelationSetModel {

    int getId();

    Set<PropertyAliasModel> getAliases(QName messageType);
}
