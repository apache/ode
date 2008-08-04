package org.apache.ode.bpel.rapi;

import java.io.OutputStream;
import java.io.IOException;

public interface Serializer {

    public void writePModel(ProcessModel process, OutputStream os) throws IOException;

    public ProcessModel readPModel() throws IOException, ClassNotFoundException;
}
