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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;

import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;

import org.apache.ode.bpel.compiler.wsdl.Definition4BPEL;


public class DefaultResourceFinder implements ResourceFinder {

    private File _suDir;

    public DefaultResourceFinder() {
        _suDir = new File("");
    }

    public DefaultResourceFinder(File suDir) {
        _suDir = suDir;
    }


    public InputStream openResource(URI uri) throws MalformedURLException, IOException {
        URI suURI = _suDir.toURI();
        URI relative = suURI.relativize(uri);
        if (relative.isAbsolute()) {
            // We don't allow absolute URIs 
            return null;
        }
        
        return new FileInputStream(new File(suURI.getPath(),relative.getPath()));
    }

}
