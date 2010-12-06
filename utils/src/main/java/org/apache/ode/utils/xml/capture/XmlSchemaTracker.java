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
 * {@link Tracker} for XML schema imports / includes.
 */
public class XmlSchemaTracker extends Tracker {
  private static final Log __log = LogFactory.getLog(XmlSchemaTracker.class);

  private static final String NS="http://www.w3.org/2001/XMLSchema" ;

  public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
    if (uri != null && uri.equals(NS) && (localName.equals("import") || localName.equals("include"))) {
      String loc = atts.getValue("schemaLocation");
      if (__log.isDebugEnabled()) {
          __log.debug("found reference element " + uri + "@" + localName + "-->" +loc);
      }

      if (loc != null) addReference(loc);
    }
  }
}
