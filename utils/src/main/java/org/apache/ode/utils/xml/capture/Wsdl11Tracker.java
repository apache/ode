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
package org.apache.ode.utils.xml.capture;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Tracker for WSDL 11 documents. This class extends the
 * {@link org.apache.ode.utils.xml.capture.XmlSchemaTracker} class because a WSDL
 * document may have in-line schemas.
 */
public class Wsdl11Tracker extends XmlSchemaTracker {
  private static final Log __log = LogFactory.getLog(Wsdl11Tracker.class);

  public static final String NS = "http://schemas.xmlsoap.org/wsdl/";

  public static final String EL_IMPORT = "import";

  public static final String ATTR_LOCATION = "location";

  public static final String ATTR_NAMESPACE = "targetNamespace";

  public void startElement(String uri, String localName, String qName,
      Attributes atts) throws SAXException {
    if (uri != null && NS.equals(uri) && EL_IMPORT.equals(localName)) {

      // We prefer the "location" attribute, but if it is missing
      // we assume the namespace is a URL.
      String schemaLoc = atts.getValue(ATTR_LOCATION);
      if (schemaLoc == null)
        schemaLoc = atts.getValue(ATTR_NAMESPACE);

      if (__log.isDebugEnabled()) {
          __log.debug("found WSDL11 reference element " + uri + "@" + localName
                  + "-->" + schemaLoc);
      }
          
      addReference(schemaLoc);
    }

    super.startElement(uri, localName, qName, atts);
  }

}
