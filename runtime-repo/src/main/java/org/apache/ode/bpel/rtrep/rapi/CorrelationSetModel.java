package org.apache.ode.bpel.rtrep.rapi;

import javax.xml.namespace.QName;
import java.util.Set;

public interface CorrelationSetModel {

    int getId();

    Set<PropertyAliasModel> getAliases(QName messageType);
}
