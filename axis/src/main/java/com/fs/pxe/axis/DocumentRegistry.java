/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.axis;

import com.fs.pxe.bom.wsdl.*;
import com.fs.pxe.bpel.capi.CompilationException;
import com.fs.utils.xsd.SchemaModel;
import com.fs.utils.xsd.SchemaModelImpl;
import com.fs.utils.xsd.XSUtils;
import com.fs.utils.xsd.XsdException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;


/**
 * A parsed collection of WSDL definitions, including BPEL-specific extensions.
 */
class DocumentRegistry {
  private static final Log __log = LogFactory.getLog(DocumentRegistry.class);
  private static final Messages __msgs = Messages.getMessages(Messages.class);

  private final HashSet<String> _loadedDefinitions = new HashSet<String>();
  private final HashMap<String, Definition4BPEL> _definitions = new HashMap<String, Definition4BPEL>();
  private final Map<URI, byte[]> _schemas = new HashMap<URI,byte[]>();

  private SchemaModel _model;
	private XMLEntityResolver _resolver;

  public DocumentRegistry(XMLEntityResolver resolver) {
    // bogus schema to force schema creation
    _schemas.put(URI.create("http://fivesight.com/bogus/namespace"),
                 ("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " targetNamespace=\"http://fivesight.com/bogus/namespace\">"
                 + "<xsd:simpleType name=\"__bogusType__\">"
                 + "<xsd:restriction base=\"xsd:normalizedString\"/>"
                 + "</xsd:simpleType>" + "</xsd:schema>").getBytes());
    _resolver = resolver;
  }


  /**
   * Obtains an WSDL definition based on its target namespace.
   *
   * @param targetNamespace
   *
   * @return WSDL definition or <code>null</code> if unavailable.
   */
  public Definition4BPEL getDefinition(String targetNamespace) {
    return _definitions.get(targetNamespace);
  }

  public Definition4BPEL[] getDefinitions(){
  	return _definitions.values().toArray(new Definition4BPEL[_definitions.size()]);
  }


  /**
   * Get the schema model (XML Schema).
   *
   * @return schema model
   */
  public SchemaModel getSchemaModel() {
    if (_model == null) {
      _model = SchemaModelImpl.newModel(_schemas);
    }

    assert _model != null;

    return _model;
  }

  /**
   * Adds a WSDL definition for use in resolving MessageType, PortType,
   * Operation and BPEL properties and property aliases
   * @param def WSDL definition
   */
  @SuppressWarnings("unchecked")
  public void addDefinition(Definition4BPEL def) throws CompilationException {
    if (def == null)
      throw new NullPointerException("def=null");

    if (__log.isDebugEnabled()) {
      __log.debug("addDefinition(" + def.getTargetNamespace() + " from " + def.getDocumentBaseURI() + ")");
    }

    // Do not load the same definition twice.
    if (_loadedDefinitions.contains(def.getDocumentBaseURI())) {
      if (__log.isDebugEnabled()) {
        __log.debug("WSDL at " + def.getDocumentBaseURI() + " will not be imported (duplicate import).");
      }
      return;
    }

    if (_definitions.containsKey(def.getTargetNamespace())) {
      // This indicates that we imported a WSDL with the same namespace from
      // two different locations. This is not an error, but should be a warning.
      if (__log.isDebugEnabled()) {
        __log.debug("WSDL at " + def.getDocumentBaseURI() + " will not be imported (duplicate import ).");
      }
      return;
    }

    _definitions.put(def.getTargetNamespace(), def);
    _loadedDefinitions.add(def.getDocumentBaseURI());


    captureSchemas(def);

    if (__log.isDebugEnabled())
      __log.debug("Processing <imports> in " + def.getDocumentBaseURI());

    for (List<Import>  imports : ((Map<String, List<Import>>)def.getImports()).values()) {
      HashSet<String> imported = new HashSet<String>();

      for (Import im : imports) {
        // If there are several imports in the same WSDL all importing the same namespace
        // that is a sure sign of programmer error.
        if (imported.contains(im.getNamespaceURI())) {
          throw new DeploymentException(__msgs.errDuplicateWSDLImport(im.getNamespaceURI(), im.getLocationURI()));

        }

        Definition4BPEL importDef = (Definition4BPEL) im.getDefinition();

        // The assumption here is that if the definition is not set on the
        // import object then there was some problem parsing the thing,
        // although it would have been nice to actually get the parse
        // error.
        if (importDef == null) {
          throw new DeploymentException(
                  __msgs.errWsdlImportNotFound(im.getNamespaceURI(), im.getLocationURI()));
        }

        imported.add(im.getNamespaceURI());
        addDefinition((Definition4BPEL) im.getDefinition());
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void captureSchemas(Definition def) throws CompilationException {
    assert def != null;

    if (__log.isDebugEnabled())
      __log.debug("Processing XSD schemas in " + def.getDocumentBaseURI());

    Types types = def.getTypes();

    if (types != null) {
      for (ExtensibilityElement ee : ((List<ExtensibilityElement>) def.getTypes().getExtensibilityElements())) {
        if (ee instanceof XMLSchemaType) {
          String schema = ((XMLSchemaType) ee).getXMLSchema();
          Map<URI, byte[]> capture;
          URI docuri;
          try {
            docuri = new URI(def.getDocumentBaseURI());
          } catch (URISyntaxException e) {
            // This is really quite unexpected..
            __log.fatal("Internal Error: WSDL Base URI is invalid.", e);
            throw new RuntimeException(e);
          }

          try {
            capture = XSUtils.captureSchema(docuri, schema, _resolver);

            // Add new schemas to our list.
            _schemas.putAll(capture);
          } catch (XsdException xsde) {
            __log.debug("captureSchemas: capture failed for " + docuri, xsde);

            LinkedList<XsdException> exceptions = new LinkedList<XsdException>();
            while (xsde != null) {
              exceptions.addFirst(xsde);
              xsde = xsde.getPrevious();
            }

            if (exceptions.size() > 0) {
              throw new DeploymentException(
                      __msgs.errSchemaError(exceptions.get(0).getDetailMessage()));
            }
          }
          // invalidate model
          _model = null;
        }
      }
    }
  }

  public Property getProperty(QName name) {
    Definition4BPEL declaringDef = getDefinition(name.getNamespaceURI());
    if (declaringDef == null) return null;
    return declaringDef.getProperty(name);
  }

  public PropertyAlias getPropertyAlias(QName propertyName, QName messageType) {
    Definition4BPEL declaringDef = getDefinition(propertyName.getNamespaceURI());
    if (declaringDef == null) return null;
    return declaringDef.getPropertyAlias(propertyName, messageType);
  }

  public PartnerLinkType getPartnerLinkType(QName partnerLinkType) {
    Definition4BPEL declaringDef = getDefinition(partnerLinkType.getNamespaceURI());
    if (declaringDef == null) return null;
    return declaringDef.getPartnerLinkType(partnerLinkType);
  }

  public PortType getPortType(QName portType) {
    Definition4BPEL declaringDef = getDefinition(portType.getNamespaceURI());
    if (declaringDef == null) return null;
    return declaringDef.getPortType(portType);
  }

}
