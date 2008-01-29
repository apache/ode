package org.apache.ode.axis2;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.engine.AxisServer;
import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;

import javax.servlet.ServletException;
import java.io.*;
import java.net.URL;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public abstract class Axis2TestBase extends TestCase {

    protected ODEAxis2Server server;

    public void start() throws Exception {
        String webappPath = getClass().getClassLoader().getResource("webapp").getFile();
        server = new ODEAxis2Server(webappPath);
        server.start();
    }

    protected class ODEAxis2Server extends AxisServer {
        ODEServer _ode = new ODEServer();
        String webappPath;

        protected ODEAxis2Server (String webappPath) throws Exception {
            super(false);
            this.webappPath = webappPath;
            String confLocation = webappPath + "/WEB-INF/conf/axis2.xml";
            String repoLocation = webappPath + "/WEB-INF/processes";
            configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(repoLocation, confLocation);
        }

        protected void start() throws AxisFault {
            super.start();
            _ode = new ODEServer();
            try {
                _ode.init(webappPath+"/WEB-INF", configContext.getAxisConfiguration());
            } catch (ServletException e) {
                e.printStackTrace();
            }
        }

        protected void deployProcess(String bundleName) {
            _ode.getProcessStore().deploy(new File(getBundleDir(bundleName)));
        }
        protected void undeployProcess(String bundleName) {
            _ode.getProcessStore().undeploy(new File(getBundleDir(bundleName)));
        }

        protected String sendRequestFile(String endpoint, String bundleName, String filename) {
            try {
                return HttpSoapSender.doSend(new URL(endpoint), new FileInputStream(getBundleDir(bundleName)+"/" + filename),
                        null, 0, null, null, null);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        protected String getBundleDir(String bundleName) {
            return getClass().getClassLoader().getResource(bundleName).getFile();
        }

        /**
         * Convenient methods to generate a WSDL for an Axis2 service. Often nice, but also often
         * generates crappy WSDL that aren't even valid (especially when faults are involved) so
         * use with care.
         * @param serviceName
         * @param fileName
         * @throws AxisFault
         */
        protected void generateWSDL(String serviceName, String fileName) throws AxisFault {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            configContext.getAxisConfiguration().getService(serviceName).printWSDL(fos);
        }
    }

}
