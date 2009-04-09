package org.apache.ode.axis2.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.Policy;
import org.apache.rampart.RampartMessageData;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.URL;
import java.net.URI;

/**
 *
 */
public class AxisUtils {

    private static final Log log = LogFactory.getLog(AxisUtils.class);

    public static void configureService(AxisService axisService, URL service_file) throws IOException, XMLStreamException, AxisFault {
        configureService(new ConfigurationContext(axisService.getAxisConfiguration()), axisService, service_file);
    }

    /**
     * Configure a service instance woth the specified service.xml document.
     * If modules are mentioned in the document, <code>this</code> method will make sure they are properly engaged and engage them if necessary.
     * The modules have to be available in the module repository otherwise an AxisFault will be thrown.
     *
     * @param axisService  the service to configure
     * @param service_file the service.xm document to configure the service with
     * @throws IOException
     * @throws XMLStreamException
     * @throws org.apache.axis2.AxisFault if a module listed in the service.xml is not available in the module repository
     */
    public static void configureService(ConfigurationContext configCtx, AxisService axisService, URL service_file) throws IOException, XMLStreamException, AxisFault {
        InputStream ais = service_file.openStream();
        log.debug("Looking for Axis2 service configuration file: " + service_file);
        if (ais != null) {
            log.debug("Configuring service " + axisService.getName() + " using: " + service_file);
            try {
                if (configCtx == null)
                    configCtx = new ConfigurationContext(axisService.getAxisConfiguration());
                ServiceBuilder builder = new ServiceBuilder(ais, configCtx, axisService);
                builder.populateService(builder.buildOM());
            } finally {
                ais.close();
            }
            // the service builder only updates the module list but do not engage them
            // modules have to be engaged manually,
            for (int i = 0; i < axisService.getModules().size(); i++) {
                String moduleRef = (String) axisService.getModules().get(i);
                AxisModule module = axisService.getAxisConfiguration().getModule(moduleRef);
                if (module != null) {
                    axisService.engageModule(module);
                } else {
                    throw new AxisFault("Unable to engage module: " + moduleRef);
                }
            }
        }
    }

    public static void applySecurityPolicy(AxisService service, String policy_file) {
        URI policyUri = new File(policy_file).toURI();
        if (log.isDebugEnabled()) log.debug("Applying security policy: " + policyUri);
        try {
            InputStream policyStream = policyUri.toURL().openStream();
            try {
                Policy policyDoc = PolicyEngine.getPolicy(policyStream);
                service.getPolicySubject().attachPolicy(policyDoc);
                // make sure the proper modules are engaged, if they are available
                engageModules(service, "rampart", "rahas");
            } finally {
                policyStream.close();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Exception while parsing policy: " + policyUri, e);
        }
    }

    public static void engageModules(AxisDescription description, String... modules) throws AxisFault {
        for (String m : modules) {
            if (description.getAxisConfiguration().getModule(m) != null) {
                if (!description.getAxisConfiguration().isEngaged(m) && !description.isEngaged(m)) {
                    description.engageModule(description.getAxisConfiguration().getModule(m));
                }
            } else {
                if (log.isDebugEnabled()) log.debug("Module " + m + " is not available.");
            }
        }
    }
}
