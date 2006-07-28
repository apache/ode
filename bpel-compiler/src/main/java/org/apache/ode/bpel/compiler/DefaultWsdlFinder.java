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

import org.apache.ode.bom.wsdl.Definition4BPEL;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;


class DefaultWsdlFinder implements WsdlFinder {
  
  private URI _base;
  
  public DefaultWsdlFinder() {
    // no base URL
  }
  
  public DefaultWsdlFinder(URI u) {
    setBaseURI(u);
  }
  
  public void setBaseURI(URI u) {
    File f = new File(u);
    if (f.exists() && f.isFile()) {
      _base = f.getParentFile().toURI();
    } else {
      _base = u;
    }
  }
 
  public Definition4BPEL loadDefinition(WSDLReader r, URI uri) throws WSDLException {
    return (Definition4BPEL) r.readWSDL(
        (_base == null?null:(_base.toASCIIString())),
        uri.toASCIIString());
  }

	public InputStream openResource(URI uri) throws MalformedURLException, IOException {
		return uri.toURL().openStream();
	}
  
  
}
