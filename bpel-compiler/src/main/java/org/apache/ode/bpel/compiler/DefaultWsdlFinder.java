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

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;


public class DefaultWsdlFinder implements WsdlFinder {

    private File _suDir;

    public DefaultWsdlFinder() {
        _suDir = new File(".");
    }

    public DefaultWsdlFinder(File suDir) {
        _suDir = suDir;
    }

    public void setBaseURI(URI u) {
        _suDir = new File(u);
    }

    public Definition4BPEL loadDefinition(WSDLReader r, URI uri) throws WSDLException {
        // Eliminating whatever path has been provided, we always look into our SU
        // deployment directory.
        String strUri = uri.toString();
//        String filename = strUri.substring(strUri.lastIndexOf("/"), strUri.length());
        return (Definition4BPEL) r.readWSDL(new File(_suDir, strUri).getPath());
    }

    public InputStream openResource(URI uri) throws MalformedURLException, IOException {
        String strUri = uri.getPath();
        return new FileInputStream(new File(_suDir, strUri));
    }

}
