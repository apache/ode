/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.sfwk.deployment.som.sax;

import com.fs.pxe.sfwk.deployment.som.Port;
import com.fs.pxe.sfwk.deployment.som.Service;
import com.fs.pxe.sfwk.deployment.som.SystemDescriptor;
import com.fs.pxe.sfwk.deployment.som.impl.Constants;
import com.fs.pxe.sfwk.deployment.som.impl.PortImpl;
import com.fs.pxe.sfwk.deployment.som.impl.SystemDescriptorImpl;
import com.fs.pxe.sfwk.rr.RepositoryWsdlLocator;
import com.fs.pxe.sfwk.rr.ResourceRepository;
import com.fs.utils.SystemConfigurationException;
import com.fs.utils.XMLParserUtils;
import com.fs.utils.sax.FailOnErrorErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.*;


/**
 * <p>
 * A generic factory class for parsing PXE system descriptors from a <code>URL</code>
 * or SAX <code>InputSource</code>.  The SAX <code>ErrorHandler</code> interface is
 * used as a generic error reporting mechanism, so errors in the internal consistency
 * of the descriptor will be reported by the <code>ErrorHandler</code> in addition to
 * errors in well-formedness or schema validity.
 * </p>
 */
public class SystemDescriptorFactory {
  
  private static final Log __log = LogFactory.getLog(SystemDescriptorFactory.class);
  
  /**
   * <p>
   * Parse the XML representation of a system descriptor into an internal
   * representation.
   * </p>
   * <p>
   * <em>Note:</em> The XML Schema for the system descriptor purposefully omits the
   * attributes (UUIDs) set by the framework at deployment time.  Thus, for 
   * descriptors that have been converted to <code>String</code> representations and
   * then are being parsed again, <code>validate</code> should be <code>false</code>,
   * or the method will fail.
   * </p>
   * 
   * @param u a <code>URL</code> from which the descriptor can be read.
   * @param eh an <code>ErrorHandler</code> to receive errors or warnings generated 
   * by the parse or <code>null</code> for default behavior (exception on error).
   * @param common a <code>ResourceRepository</code> from which the root WSDL can
   * be obtained or <code>null</code> if none is available (or relevant).
   * @param validate whether or not to perform XML Schema validation of the input.
   * @return the <code>SystemDescriptor</code> created from the input stream.
   * @throws IOException if one occurs while reading the <code>URL</code>
   * @throws SAXException if one occurs while parsing, including those thrown as a
   * result of validation errors or the handling of non-fatal errors by the
   * <code>ErrorHandler</code>
   */
  public static SystemDescriptor parseDescriptor(URL u, ErrorHandler eh, 
      ResourceRepository common, boolean validate) throws IOException, SAXException
  {
    InputStream is = u.openStream();
    InputSource isrc = new InputSource(is);
    isrc.setSystemId(u.toExternalForm());

    try {
      return parseDescriptor(isrc, eh, common, validate);
    } finally {
      is.close();
    }
  }
    
  /**
   * <p>
   * Parse the XML representation of a system descriptor into an internal
   * representation.
   * </p>
   * <p>
   * <em>Note:</em> The XML Schema for the system descriptor purposefully omits the
   * attributes (UUIDs) set by the framework at deployment time.  Thus, for 
   * descriptors that have been converted to <code>String</code> representations and
   * then are being parsed again, <code>validate</code> should be <code>false</code>,
   * or the method will fail.
   * </p>
   * @param is an <code>InputSource</code> containing the desciptor to be parsed.
   * @param eh an <code>ErrorHandler</code> to receive errors or warnings generated 
   * by the parse or <code>null</code> for default behavior (exception on error)
   * @param common a <code>ResourceRepository</code> from which the root WSDL can
   * be obtained or <code>null</code> if none is available (or relevant).
   * @param validate whether or not to perform XML Schema validation of the input.
   * @return the <code>SystemDescriptor</code> created from the input stream.
   * @throws IOException if one occurs while reading the <code>InputSource</code>
   * @throws SAXException if one occurs while parsing, including those thrown as a
   * result of validation errors or the handling of non-fatal errors by the
   * <code>ErrorHandler</code>
   */
  public static SystemDescriptor parseDescriptor(InputSource is, ErrorHandler eh, 
      ResourceRepository common, boolean validate) throws IOException,SAXException
  {
    if (eh == null) {
      eh = new FailOnErrorErrorHandler();
    }

    if (is.getSystemId() == null) {
      __log.warn("A system identifier should be supplied for error reporting purposes.");
      is.setSystemId("<<null>>");
    }

    XMLReader xr = XMLParserUtils.getXMLReader();
    XMLParserUtils.setNamespaces(xr);
    if (validate) {
      try {
        XMLParserUtils.setExternalSchemaURL(xr,Constants.DESCRIPTOR_URI,
            getSystemDescriptorSchema().toExternalForm());
      } catch (SAXException saxe) {
        // TODO: Error message needed.
        __log.error("",saxe);
        throw saxe;
      }
    }
    xr.setErrorHandler(eh);
    SystemDescriptorContentHandler scdh = new SystemDescriptorContentHandler(eh);
    xr.setContentHandler(scdh);
    xr.parse(is);
    SystemDescriptorImpl sd = scdh.getDescriptor();
    if (validate) {
      postValidate(sd,eh,common);
    }
    return sd;
  }
  
  private static void crunchPorts(ErrorHandler eh, Service ss, Port[] pp, Map<String, QName> ports) 
      throws SAXException
  {
    for (int j=0; j < pp.length; ++j) {
      String cr = pp[j].getChannelRef();
      QName type = ports.get(cr);
      if (type == null) {
        ports.put(cr,pp[j].getType());
      } else {
        if (!pp[j].getType().equals(type)) {
          eh.error(new SAXParseException("Other end of channel " + cr + " is " +  
              type.toString() + " instead of " +
              pp[j].getType().toString() + " for port " + pp[j].getName() +
              " on service " + ss.getName(),
              (PortImpl)pp[j]));            
        }
      }
    }
  }
  
  /*
   * The schema is designed so that most of the cross-reference checking can be
   * done by the schema processor and not by this class.  This includes:
   * 
   * - Uniqueness of service names.
   * - Uniqueness of port names per service.
   * - Correct parity of ports on channels (import-import, export-export).
   * 
   * The only things that can't be checked by the schema are:
   * 
   * - Presence of the WSDL for the system in the common repository.
   * - Matching types for channel ends.
   * - Presence of the portTypes in the WSDL for the system.
   */
  private static void postValidate(SystemDescriptorImpl sd, ErrorHandler eh,
      ResourceRepository rr) throws SAXException  
  {
    Map<String, QName> ports = new HashMap<String, QName>();
    Service[] ss = sd.getServices();
    
    for (int i=0; i < ss.length; ++i) {
      crunchPorts(eh,ss[i],ss[i].getImportedPorts(),ports);
      crunchPorts(eh,ss[i],ss[i].getExportedPorts(),ports);
    }
    
    if (rr == null) {
      eh.warning(new SAXParseException("No resource repository specified; type cross-references not checked.",null));
      return;
    }

    InputStream is = null;
    try {
      is = rr.resolveURI(sd.getWsdlUri()).openStream();
    } catch (Exception ex) {
      eh.error(new SAXParseException("Root WSDL URI " + sd.getWsdlUri() + " not found in resource repository.", null));
      return;
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException ioex) {
          // ignore
        }
      }
    }

    RepositoryWsdlLocator loc = new RepositoryWsdlLocator(rr, sd.getWsdlUri());
    WSDLReader wsdlReader = null;

    try {
      wsdlReader = WSDLFactory.newInstance().newWSDLReader();
      wsdlReader.setFeature("javax.wsdl.verbose", false);
    } catch (WSDLException wsdlEx) {
      throw new SAXException(wsdlEx);
    }

    Definition def = null;
    try {
      def = wsdlReader.readWSDL(loc);
    } catch (WSDLException wsdlEx) {
      eh.error(new SAXParseException(wsdlEx.toString(),null,wsdlEx));
      return;
    }

    for (int i=0; i < ss.length; ++i) {
      postValidatePorts(ss[i],ss[i].getImportedPorts(),def,eh);
      postValidatePorts(ss[i],ss[i].getExportedPorts(),def,eh);
    }
  }
  
  private static void postValidatePorts(Service s, Port[] pp, Definition d,
      ErrorHandler eh) throws SAXException
  {
    for (int j=0; j<pp.length; ++j) {
      QName qn = pp[j].getType();
      if (d.getPortType(qn) == null) {
        eh.error(
            new SAXParseException(
                "Type " + qn.toString() + " for port " + pp[j].getName() + 
                " on " + s.getName() + " not declared in root WSDL.",
                ((PortImpl)pp[j]))
                );
      }
    }
  }
  
  /**
   * Retrieve the system descriptor schema from the classpath.
   * @return a <code>URL</code> containing the schema.
   */
  public static URL getSystemDescriptorSchema() {
    URL u = SystemDescriptorFactory.class.getResource("system-descriptor.xsd");
    if (u == null) {
      throw new SystemConfigurationException("Expected resource \"system-descriptor.xsd\" was not on the classpath.");
    }
    return u; 
  }
}
