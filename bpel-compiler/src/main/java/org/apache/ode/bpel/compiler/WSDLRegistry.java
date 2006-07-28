/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.wsdl.*;
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.capi.CompilerContext;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.ode.utils.xsd.SchemaModel;
import org.apache.ode.utils.xsd.SchemaModelImpl;
import org.apache.ode.utils.xsd.XSUtils;
import org.apache.ode.utils.xsd.XsdException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A parsed collection of WSDL definitions, including BPEL-specific extensions.
 */
class WSDLRegistry {
  private static final Log __log = LogFactory.getLog(WSDLRegistry.class);

  private static final CommonCompilationMessages __cmsgs =
    MessageBundle.getMessages(CommonCompilationMessages.class);

  private final HashSet<String> _loadedDefinitions = new HashSet<String>();

  private final HashMap<String, Definition4BPEL> _definitions = new HashMap<String, Definition4BPEL>();
  
  private final Map<URI, byte[]> _schemas = new HashMap<URI,byte[]>();

  private SchemaModel _model;

	private XMLEntityResolver _resolver;
	private CompilerContext _ctx;


  WSDLRegistry(WsdlFinder finder, CompilerContext cc) {
    // bogus schema to force schema creation
    _schemas.put(URI.create("http://fivesight.com/bogus/namespace"),
                 ("<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\""
                 + " targetNamespace=\"http://fivesight.com/bogus/namespace\">"
                 + "<xsd:simpleType name=\"__bogusType__\">"
                 + "<xsd:restriction base=\"xsd:normalizedString\"/>"
                 + "</xsd:simpleType>" + "</xsd:schema>").getBytes());
    if (finder != null)
    	_resolver = new WsdlFinderXMLEntityResolver(finder);
    
    _ctx = cc;
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
   *
   * @param def WSDL definition
   *
   * @throws WSDLException 
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

      for (int i = 0; i < imports.size(); ++i) {
        Import im = imports.get(i);
        
        // If there are several imports in the same WSDL all importing the same namespace
        // that is a sure sign of programmer error. 
        if (imported.contains(im.getNamespaceURI())) {
    			CompilationException ce = 
            new CompilationException(__cmsgs.errDuplicateWSDLImport(im.getNamespaceURI(),im.getLocationURI()).setSource(def.getDocumentBaseURI()));
    			if (_ctx == null)
    				throw ce;
    			else 
    				_ctx.recoveredFromError(def.getDocumentBaseURI(),ce);
          
          continue;
        }
        
        Definition4BPEL importDef = (Definition4BPEL) im.getDefinition();
        
        // The assumption here is that if the definition is not set on the
        // import object then there was some problem parsing the thing, 
        // although it would have been nice to actually get the parse 
        // error.
        if (importDef == null) {
          CompilationException ce = new CompilationException(
              __cmsgs.errWsdlImportNotFound(im.getNamespaceURI(),
                  im.getLocationURI()).setSource(def.getDocumentBaseURI()));
          if (_ctx == null)
            throw ce;
          else 
            _ctx.recoveredFromError(def.getDocumentBaseURI(),ce);
          
          continue;
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
      for (Iterator<ExtensibilityElement> iter =
        ((List<ExtensibilityElement>)def.getTypes().getExtensibilityElements()).iterator();
        iter.hasNext();) {
        ExtensibilityElement ee = iter.next();

        if (ee instanceof XMLSchemaType) {
          String schema = ((XMLSchemaType)ee).getXMLSchema();
          Map<URI, byte[]> capture = null;
          URI docuri;
					try {
						docuri = new URI(def.getDocumentBaseURI());
					} catch (URISyntaxException e) {
						// This is really quite unexpected..
						__log.fatal("Internal Error: WSDL Base URI is invalid.",e);
						throw new RuntimeException(e);
					}

					try {
            capture = XSUtils.captureSchema(docuri, schema, _resolver);

            // Add new schemas to our list.                         
            _schemas.putAll(capture);

          } catch (XsdException xsde) {
            __log.debug("captureSchemas: capture failed for " + docuri,xsde);

            LinkedList<XsdException> exceptions = new LinkedList<XsdException>();
            while (xsde != null)  {
              exceptions.addFirst(xsde);
              xsde = xsde.getPrevious();
            }
            
            for (XsdException ex : exceptions) {
              // TODO: the line number here is going to be wrong for the in-line schema.
            	String location = ex.getSystemId() + ":"  + ex.getLineNumber();
            	CompilationException ce = new CompilationException(
            			__cmsgs.errSchemaError(ex.getDetailMessage()).setSource(location));
            	if (_ctx != null)
            		_ctx.recoveredFromError(location,ce);
            	else
            		throw ce;
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
