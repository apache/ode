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

package org.apache.ode.bpel.engine;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;



/**
 * Class for generating information about a document resource.
 */
class DocumentInfoGenerator {
  private final File _file;
  private String _type;

  private static final Map<String, String> __extToTypeMap = new HashMap<String,String>();
  static {
    // Assume WSDL is 1.1 for now...
    __extToTypeMap.put(".wsdl", "http://schemas.xmlsoap.org/wsdl/");

    __extToTypeMap.put(".xsd",  "http://www.w3.org/2001/XMLSchema");
    __extToTypeMap.put(".svg",  "http://www.w3.org/2000/svg");
    __extToTypeMap.put(".cbp",  "http://ode.apache.org/schemas/2005/12/19/CompiledBPEL");
    // Assume BPEL is 2.0 for now...
    __extToTypeMap.put(".bpel", "http://schemas.xmlsoap.org/ws/2004/03/business-process/");
  }


  DocumentInfoGenerator(File f) {
    _file = f;

    recognize();
  }


  public boolean isRecognized() {
    return _type != null;
  }

  public boolean isVisible() {
    return !_file.isHidden();
  }

  public String getName() {
    return _file.getName();
  }

  public String getURL() {
    try {
      return _file.toURL().toExternalForm();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public String getType() {
    return _type;
  }

  private void recognize() {
    String fname = _file.getName().toLowerCase();

    for (Map.Entry<String,String>i:__extToTypeMap.entrySet()) {
      if (fname.endsWith(i.getKey().toLowerCase())) {
        _type = i.getValue();
        break;
      }
    }
  }
}
