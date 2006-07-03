/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.jbi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.schema.SchemaImport;
import javax.wsdl.factory.WSDLFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class WSDLUtils {

  public static Document toDOM(Definition definition) throws WSDLException,
      TransformerFactoryConfigurationError, TransformerException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    WSDLFactory.newInstance().newWSDLWriter().writeWSDL(definition, baos);
    Transformer t = TransformerFactory.newInstance().newTransformer();
    DOMResult result = new DOMResult();
    t.transform(new StreamSource(new ByteArrayInputStream(baos.toByteArray())),
        result);
    return (Document) result.getNode();
  }

  public static Definition flattenWsdl(Definition definition)
      throws WSDLException {
    if (definition.getImports().size() == 0) {
      return definition;
    } else {
      Definition newDef = WSDLFactory.newInstance().newDefinition();
      newDef.setTargetNamespace(definition.getTargetNamespace());
      newDef.setQName(definition.getQName());
      mergeWsdl(newDef, definition, true);
      return newDef;
    }
  }

  protected static void mergeWsdl(Definition dest, Definition src, boolean main) {
    Map imports = src.getImports();
    for (Iterator it = imports.values().iterator(); it.hasNext();) {
      List l = (List) it.next();
      for (Iterator itL = l.iterator(); itL.hasNext();) {
        Import imp = (Import) itL.next();
        mergeWsdl(dest, imp.getDefinition(), false);
      }
    }
    Map namespaces = src.getNamespaces();
    for (Iterator iter = namespaces.keySet().iterator(); iter.hasNext();) {
      String prefix = (String) iter.next();
      dest.addNamespace(prefix, (String) namespaces.get(prefix));
    }

    Map bindings = src.getBindings();
    for (Iterator iter = bindings.values().iterator(); iter.hasNext();) {
      Binding binding = (Binding) iter.next();
      dest.addBinding(binding);
    }
    Map messages = src.getMessages();
    for (Iterator iter = messages.values().iterator(); iter.hasNext();) {
      Message message = (Message) iter.next();
      dest.addMessage(message);
    }
    Map portTypes = src.getPortTypes();
    for (Iterator iter = portTypes.values().iterator(); iter.hasNext();) {
      PortType portType = (PortType) iter.next();
      dest.addPortType(portType);
    }
    if (src.getTypes() != null) {
      if (dest.getTypes() == null) {
        dest.setTypes(dest.createTypes());
      }
      mergeTypes(dest.getTypes(), src.getTypes());
    }
    if (main) {
      Map services = src.getServices();
      for (Iterator iter = services.values().iterator(); iter.hasNext();) {
        Service service = (Service) iter.next();
        dest.addService(service);
      }
    }
  }

  protected static void mergeTypes(Types dest, Types src) {
    List elements = src.getExtensibilityElements();
    for (Iterator iter = elements.iterator(); iter.hasNext();) {
      ExtensibilityElement element = (ExtensibilityElement) iter.next();
      if (element instanceof Schema) {
        Schema schema = (Schema) element;
        mergeSchema(dest, schema);
      } else {
        dest.addExtensibilityElement(element);
      }
    }
  }

  protected static void mergeSchema(Types dest, Schema schema) {
    if (schema.getImports().size() > 0) {
      for (Iterator it = schema.getImports().values().iterator(); it.hasNext();) {
        List l = (List) it.next();
        for (Iterator iter = l.iterator(); iter.hasNext();) {
          SchemaImport imp = (SchemaImport) iter.next();
          mergeSchema(dest, imp.getReferencedSchema());
        }
      }
    } else {
      List elems = dest.getExtensibilityElements();
      for (Iterator iter = elems.iterator(); iter.hasNext();) {
        ExtensibilityElement element = (ExtensibilityElement) iter.next();
        if (element instanceof Schema) {
          if (((Schema) element).getDocumentBaseURI().equals(
              schema.getDocumentBaseURI())) {
            // This schema has already been added
            return;
          }
        }
      }
      dest.addExtensibilityElement(schema);
    }
  }

}
