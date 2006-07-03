/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.elang.xpath10.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaxen.NamespaceContext;

/**
 * Class adapting the {@link javax.xml.namespace.NamespaceContext} interface to the
 * JAXEN {@link NamespaceContext} interface.
 * @pattern Adapter
 */
class JaxenNamespaceContextAdapter implements NamespaceContext {
  private static final Log __log = LogFactory.getLog(JaxenNamespaceContextAdapter.class);

  private javax.xml.namespace.NamespaceContext _namespaceContext;

  JaxenNamespaceContextAdapter(javax.xml.namespace.NamespaceContext ctx) {
    _namespaceContext = ctx;
  }

  public String translateNamespacePrefixToUri(String prefix) {
    String namespaceURI = _namespaceContext.getNamespaceURI(prefix);
    if (__log.isDebugEnabled()) {
      __log.debug("translateNamespacePrefixToUri(" + prefix + ")=" + namespaceURI);
    }
    return namespaceURI;
  }
}
