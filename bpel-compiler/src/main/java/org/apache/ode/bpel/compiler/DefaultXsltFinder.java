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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.StreamUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;

public class DefaultXsltFinder implements XsltFinder {

    private static final Log __log = LogFactory.getLog(DefaultXsltFinder.class);

    private File _suDir;

    public DefaultXsltFinder() {
      _suDir = new File(".");
    }

    public DefaultXsltFinder(File suDir) {
      _suDir = suDir;
    }

    public void setBaseURI(URI u) {
      _suDir = new File(u);
    }

    public String loadXsltSheet(URI uri) {
      // Eliminating whatever path has been provided, we always look into our SU
      // deployment directory.
      String strUri = uri.toString();
//      String filename = strUri.substring(strUri.lastIndexOf("/"), strUri.length());
      try {
        return new String(StreamUtils.read(new FileInputStream(new File(_suDir, strUri))));
      } catch (IOException e) {
        if (DefaultXsltFinder.__log.isDebugEnabled())
          DefaultXsltFinder.__log.debug("error obtaining resource '" + uri + "' from repository.", e);
        return null;
      }
    }

}
