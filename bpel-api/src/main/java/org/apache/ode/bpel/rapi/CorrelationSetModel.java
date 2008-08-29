package org.apache.ode.bpel.rapi;

import javax.xml.namespace.QName;
import java.util.List;

public interface CorrelationSetModel {

    int getId();

    List<PropertyAliasModel> getAliases(QName messageType);
}
