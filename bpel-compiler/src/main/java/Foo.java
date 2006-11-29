import java.io.FileInputStream;

import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.Serializer;

public class Foo {

    public static void main(String args[]) throws Exception {
        Serializer s = new Serializer(new FileInputStream("/home/mszefler/dev/tomcat/processes/IDAS/IDASProcess.cbp"));
        OProcess o = s.readOProcess();
        System.err.println(o);
    }
}