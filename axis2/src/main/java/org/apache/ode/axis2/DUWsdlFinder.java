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

package org.apache.ode.axis2;

import org.apache.ode.bom.wsdl.Definition4BPEL;
import org.apache.ode.bpel.compiler.WsdlFinder;

import javax.wsdl.xml.WSDLReader;
import javax.wsdl.WSDLException;
import java.net.URI;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 * Finds WSDL documents within a deployment unit (no relative path shit,
 * everything's in the same directory).
 */
public class DUWsdlFinder implements WsdlFinder {

  private File _suDir;

  public DUWsdlFinder() {
    // no base URL
  }

  public DUWsdlFinder(File suDir) {
    _suDir = suDir;
  }

  public void setBaseURI(URI u) {
    _suDir = new File(u);
  }

  public Definition4BPEL loadDefinition(WSDLReader r, URI uri) throws WSDLException {
    // Eliminating whatever path has been provided, we always look into our SU
    // deployment directory.
    String strUri = uri.toString();
    String filename = strUri.substring(strUri.lastIndexOf("/"), strUri.length());
    return (Definition4BPEL) r.readWSDL(new File(_suDir, filename).getPath());
  }

  public InputStream openResource(URI uri) throws MalformedURLException, IOException {
    return uri.toURL().openStream();
  }

}
