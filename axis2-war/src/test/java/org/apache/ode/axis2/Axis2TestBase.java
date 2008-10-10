package org.apache.ode.axis2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.AxisServer;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.util.Axis2UriResolver;
import org.apache.ode.axis2.util.Axis2WSDLLocator;
import org.apache.ode.tools.sendsoap.cline.HttpSoapSender;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import javax.servlet.ServletException;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public abstract class Axis2TestBase {

    private static final int DEFAULT_TEST_PORT = 8888;
    
    protected ODEAxis2Server server;

    // Provide standard constructors to accommodate creation of test suites
    public Axis2TestBase(String name) {
    }
    
    public Axis2TestBase() {
    	this(null);
    }
    
    public void startServer() throws Exception {
        String webappPath = getClass().getClassLoader().getResource("webapp").getFile();
        server = new ODEAxis2Server(webappPath);
        server.start();
    }

    public void stopServer() throws AxisFault {
        server.stop();
    }

  @BeforeMethod
    protected void setUp() throws Exception {
        startServer();
    }

  @AfterMethod
    protected void tearDown() throws Exception {
        stopServer();
    }

    protected class ODEAxis2Server extends AxisServer {
        ODEServer _ode = new ODEServer();
        String webappPath;

        protected ODEAxis2Server (String webappPath) throws Exception {
            super(false);
            this.webappPath = webappPath;
            String confLocation = webappPath + "/WEB-INF/conf/axis2.xml";
            String repoLocation = webappPath + "/WEB-INF";
            configContext = ConfigurationContextFactory
                    .createConfigurationContextFromFileSystem(repoLocation, confLocation);
            // do not use 8080 for tests
            configContext.getAxisConfiguration().getTransportIn("http").addParameter(new Parameter("port", ""+DEFAULT_TEST_PORT));
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

        public void stop() throws AxisFault {
            _ode.shutDown();
            _ode = null;
            super.stop();
        }

        public Collection<QName> deployProcess(String bundleName) {
            return _ode.getProcessStore().deploy(new File(getBundleDir(bundleName)));
        }
        public void undeployProcess(String bundleName) {
            _ode.getProcessStore().undeploy(new File(getBundleDir(bundleName)));
        }
        public boolean isDeployed(String bundleName) {
            return _ode.getProcessStore().getPackages().contains(bundleName);
        }

        /**
         * Creates and deploys an Axis service based on a provided MessageReceiver. The receiver
         * will be invoked for all invocations of that service.
         */
        protected void deployService(String bundleName, String defFile, QName serviceName, String port,
                                     MessageReceiver receiver) throws WSDLException, IOException, URISyntaxException {
            URI wsdlUri = new File(getBundleDir(bundleName) + "/" + defFile).toURI();

            InputStream is = wsdlUri.toURL().openStream();
            WSDL11ToAxisServiceBuilder serviceBuilder = new ODEAxisService.WSDL11ToAxisPatchedBuilder(is, serviceName, port);
            serviceBuilder.setBaseUri(wsdlUri.toString());
            serviceBuilder.setCustomResolver(new Axis2UriResolver());
            serviceBuilder.setCustomWSLD4JResolver(new Axis2WSDLLocator(wsdlUri));
            serviceBuilder.setServerSide(true);

            AxisService axisService = serviceBuilder.populateService();
            axisService.setName(serviceName.getLocalPart());
            axisService.setWsdlFound(true);
            axisService.setCustomWsdl(true);
            axisService.setClassLoader(getConfigurationContext().getAxisConfiguration().getServiceClassLoader());

            Iterator operations = axisService.getOperations();
            while (operations.hasNext()) {
                AxisOperation operation = (AxisOperation) operations.next();
                if (operation.getMessageReceiver() == null) {
                    operation.setMessageReceiver(receiver);
                }
            }
            getConfigurationContext().getAxisConfiguration().addService(axisService);            
        }

        public String sendRequestFile(String endpoint, String bundleName, String filename) {
            try {
                return HttpSoapSender.doSend(new URL(endpoint),
                        new FileInputStream(getBundleDir(bundleName)+"/" + filename), null, 0, null, null, null);
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

        public ODEServer getODEServer() {
            return _ode;
        }
    }

}
