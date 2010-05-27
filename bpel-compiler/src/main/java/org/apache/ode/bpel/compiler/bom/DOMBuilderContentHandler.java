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
package org.apache.ode.bpel.compiler.bom;

import org.apache.xml.utils.DOMBuilder;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class DOMBuilderContentHandler extends DOMBuilder {
    private Locator _locator;

    private int _suppressLineNo = 0;

    public DOMBuilderContentHandler(Document doc) {
        super(doc);

    }

    @Override
    public void startElement(String ns, String localName, String name, Attributes atts) throws SAXException {
        if (localName.equals("literal") || _suppressLineNo > 0)
            ++_suppressLineNo;

        if (_locator != null && _suppressLineNo == 0) {
            AttributesImpl a = new AttributesImpl(atts);
            a.addAttribute(BpelObject.ATTR_LINENO.getNamespaceURI(), BpelObject.ATTR_LINENO.getLocalPart(),
                    "odebpelc:"+BpelObject.ATTR_LINENO.getLocalPart(), "CDATA", "" + _locator.getLineNumber());
            atts = a;
        }
        super.startElement(ns, localName, name, atts);
    }


    @Override
    public void endElement(String ns, String localName, String name) throws SAXException {
        if (_suppressLineNo > 0)
            --_suppressLineNo;

        super.endElement(ns, localName, name);

    }

    @Override
    public void setDocumentLocator(Locator locator) {
        _locator = locator;
        super.setDocumentLocator(locator);
    }

}
