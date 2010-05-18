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
package org.apache.ode.bpel.elang.xpath10.runtime;

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
