package org.apache.ode.bpel.rapi;

import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.IOException;

public interface Serializer {

    public String getGuid();

    public QName getType();

    public void writePModel(ProcessModel process, OutputStream os) throws IOException;

    public ProcessModel readPModel() throws IOException, ClassNotFoundException;

}
