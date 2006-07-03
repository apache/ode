/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.xcontrib.sp.xslt;

import com.fs.pxe.sfwk.spi.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <p>
 * A simple XSLT service provider implementation.  Allows the association
 * of a single stylesheet per service deployed in this service provider, which
 * is configured in the service configuration using the 'xsltUri' service property.  
 * </p>
 * <p>
 * RESTRICTION: the input and ouptut wsdl message must have either one part, or
 * if multiple parts, the part to be transformed must be named 'content'.
 * Otherwise, the WSDL ServicePort, PortType, or operation do not matter.
 * </p>
 */
public class XSLTServiceProvider implements ServiceProvider {
	/**
	 * Deployment Property: WSDL part name, if multiple parts for a WSDL message.
	 */
	public static final String DEFAULT_PART_NAME = "content";

	/**
   * <p>
	 * Service provider property to specify the location of the stylesheet.  If the
   * specified location does not appear to be a URL, it is treated as the name of a
   * resource inside the deployment bundle.
   * </p>
	 */
	public static final String SVC_STYLESHEET_PROP = "stylesheet";

	private static final Log __log = LogFactory.getLog(XSLTServiceProvider.class);

  private ServiceProviderContext _config;
	private boolean _running = false;

  /* Cache of pre-compiled templates */
	private Map<String, Templates> _xsltCache = new HashMap<String, Templates>();

  /**
	 * Required empty constructor
	 */
	public XSLTServiceProvider() {
		super();
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#isRunning()
	 */
	public boolean isRunning() throws ServiceProviderException {
		return _running;
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#activateService(com.fs.pxe.sfwk.spi.ServiceContext)
	 */
	public void activateService(ServiceContext service) throws ServiceProviderException {
		if (__log.isDebugEnabled()) {
			__log.debug("Activating service: " + service.getServiceName());
		}
		
    String ss = service.getDeploymentProperty(SVC_STYLESHEET_PROP);
    
    if (ss == null) {
      String msg = "The property \"" + SVC_STYLESHEET_PROP + "\" must be specified.";
      __log.error(msg);
      throw new ServiceProviderException(msg);
    }
    
    /*
     * Look up the stylesheet by URL in the common resource bundle.
     */
    try {
      URL u;
      try {
        u = new URL(ss);
      } catch (MalformedURLException mue) {
        File file = new File(service.getDeployDir(),ss);
        u = file.toURL();
      }

      Templates xsltTemplate;
      InputStream is = u.openStream();
      try {
        StreamSource src = new StreamSource(is);
        xsltTemplate =
           TransformerFactory.newInstance().newTemplates(src);
      } finally {
        is.close();
      }

      /* xslt stylesheet are re-usable, so we pre-parse and cache the template */
			_xsltCache.put(service.getServiceUUID(), xsltTemplate);
    } catch (IOException e) {
      String msg = "IOException reading stylsheet \"" + ss + "\"; reason: " +
          e.getMessage();
      __log.error(msg, e);
      throw new ServiceProviderException(msg, e);
    } catch (TransformerConfigurationException e) {
      String msg = "Unable to create Templates for stylesheet \"" + ss +
          "\"; reason: " + e.getMessageAndLocation(); 
			__log.error(msg, e);
			throw new ServiceProviderException(msg, e);
		}
    if (__log.isDebugEnabled()) {
      __log.debug("Service '" + service.getServiceName()
          + "' deployed with transformation '" + ss + "'");
    }
    
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#deactivateService(com.fs.pxe.sfwk.spi.ServiceContext)
	 */
	public void deactivateService(ServiceContext service)
			throws ServiceProviderException {
		if (__log.isDebugEnabled()) {
			__log.debug("Deactivating service: " + service.getServiceName());
		}
		// NOTHING TO DO
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#deployService(com.fs.pxe.sfwk.spi.ServiceConfig)
	 */
	public void deployService(ServiceConfig service) throws ServiceProviderException {
		if (__log.isDebugEnabled()) {
			__log.debug("Deploying service: " + service.getServiceName());
		}
		// NOTHING TO DO
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#start()
	 */
	public void start() throws ServiceProviderException {
		_running = true;
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#stop()
	 */
	public void stop() throws ServiceProviderException {
		_running = false;
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#undeployService(com.fs.pxe.sfwk.spi.ServiceConfig)
	 */
	public void undeployService(ServiceConfig service) throws ServiceProviderException {
		if (__log.isDebugEnabled()) {
			__log.debug("Undeploying service: " + service.getServiceName());
		}
		// NOTHING TO DO
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#createInteractionHandler(java.lang.Class)
	 */
	public InteractionHandler createInteractionHandler(Class interactionClass)
			throws ServiceProviderException {
    // THIS WOULD BE IMPLEMENTED FOR 'FOR-OUT-BAND' INTERACTION WITH THE SERVICE PROVIDER, 
    // SUCH AS REMOTE MANAGEMENT OR INTROSPECTION USING A DYNAMIC PROXY INTERFACE
		throw new UnsupportedOperationException();
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#initialize(com.fs.pxe.sfwk.spi.ServiceProviderContext)
	 */
	public void initialize(ServiceProviderContext context)
			throws ServiceProviderException {
		_config = context;
	}

  /**
   * @see com.fs.pxe.sfwk.spi.ServiceProvider#onServiceEvent(com.fs.pxe.sfwk.spi.ServiceEvent)
   */
  public void onServiceEvent(ServiceEvent serviceEvent) throws ServiceProviderException, MessageExchangeException {
    if (serviceEvent instanceof MessageExchangeEvent){
      onMessageExchangeEvent((MessageExchangeEvent) serviceEvent);
    } else {
      throw new IllegalArgumentException("Unexpected event type of type '" + serviceEvent.getClass().getName() 
          + "'.  Excpeted " + MessageExchangeEvent.class.getName());
    }
  }

  /**
   * This does all the work of handling a message exchange event and returning 
   * the transformed xml.
   * 
   * @param mexEvent
   * @throws ServiceProviderException
   * @throws MessageExchangeException
   */
	private void onMessageExchangeEvent(MessageExchangeEvent mexEvent)
          throws ServiceProviderException, MessageExchangeException {
    ServiceContext service = mexEvent.getTargetService();
		
    /* obtain the stylesheet template for this service */
    Templates templates = _xsltCache.get(service.getServiceUUID());
		if(templates == null){
      // this should not be null, but check anyway
			throw new ServiceProviderException("No stylesheet configured for service '" + service.getServiceName() + "'.");
    }
    
    Transformer transformer;
		try {
      /* create an XSLT transformer */
			transformer = templates.newTransformer();
		} catch (TransformerConfigurationException e) {
      // shouldn't happen
			throw new RuntimeException(e);
		}
    
		switch (mexEvent.getEventType()) {
      
			case MessageExchangeEvent.IN_RCVD_EVENT :
        /* this is the only event type we should excpet to receive */
				InputRcvdMessageExchangeEvent evt = (InputRcvdMessageExchangeEvent) mexEvent;
      
        /* the message exchange has the input data, and will accept the output data */
				MessageExchange mex = evt.getMessageExchange();
        
				/* retrieves the input message placed on the exchange */
				Message inMsg = mex.lastInput();
				/* 
         * we can't really transform the whole message (WSDL message), only a 
         * a part (WSDL part), so we use use the logic of the following method.
				 */
				String partName = getPartName(inMsg.getDescription());
        
        /* Here we do the transformation using a dom input with a dom output */
				DOMSource src = new DOMSource(inMsg.getPart(partName));
				DOMResult result = new DOMResult();
				String failureMsg = null;
				try {
					transformer.transform(src, result);
				} catch (Exception  e) {
					failureMsg = "Failure during transformation: " + e.getMessage();
					__log.error(failureMsg, e);
				} 
        
				Element outElement = null;
				/* if no error, obtain the root element node of the result */
        if (failureMsg == null) {
					Node n = result.getNode();
					if (n.getNodeType() == Node.DOCUMENT_NODE) {
						outElement = ((Document) n).getDocumentElement();
					} else if (n.getNodeType() == Node.ELEMENT_NODE) {
						outElement = (Element) n;
					} else {
						failureMsg = "Transformation result was not an DOM Element or Document type.";
					}
				}
        
				if (failureMsg == null) {
          /* with no error, we create an output message for the message exchange */
					Message outMsg = mex.createOutputMessage();
					String part = getPartName(outMsg.getDescription());
					outMsg.setPart(part, outElement);
					mex.output(outMsg);
				} else {
          // if a failure occurs, report the error to the message exchange
					mex.failure(failureMsg);
				}
				break;
			case MessageExchangeEvent.FAILURE :
				// Ignore.
				break;
			default :
				__log.debug("Unexpected messageExchangeEvent: " + mexEvent);
		}
	}
	/**
	 * This logic is based upon the class documentation
	 */
	private String getPartName(javax.wsdl.Message msgType) {
		String partName = null;
		
    if (msgType.getParts().size() > 1) {
			partName = DEFAULT_PART_NAME;
		} else if (msgType.getParts().size() == 1) {
			partName = (String) msgType.getParts().keySet().iterator().next();
		} else {
			// should never get here; wsdl parser should complain long before now
			throw new IllegalStateException("Must have at least one part.");
		}
		return partName;
	}
	/**
	 * @see com.fs.pxe.sfwk.spi.ServiceProvider#getProviderURI()
	 */
	public String getProviderURI() {
		return _config.getProviderURI();
	}

}
