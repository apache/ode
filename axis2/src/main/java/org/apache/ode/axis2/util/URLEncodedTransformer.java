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

package org.apache.ode.axis2.util;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.axis2.httpbinding.Messages;
import org.w3c.dom.Element;

import javax.wsdl.Part;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class URLEncodedTransformer {


    private static final Log log = LogFactory.getLog(URLEncodedTransformer.class);

    /**
     * @param values - a map<String, Element>, the key is a part name (without curly braces), the value the replacement value for the part name. If the value is not a simple type, it will be skipped.
     * @return the encoded params
     */
    public String transform(Map<String, Element> values) {
        if (values.isEmpty()) return null;
        List<NameValuePair> l = new ArrayList<NameValuePair>(values.size());
        for (Map.Entry<String, Element> e : values.entrySet()) {
            String partName = e.getKey();
            Element value = e.getValue();
            String textValue;
            if (DOMUtils.isEmptyElement(value)) {
                textValue = "";
            } else {
                /*
                The expected part value could be a simple type
                or an element of a simple type.
                So if a element is there, take its text content
                else take the text content of the part element itself
                */
                Element childElement = DOMUtils.getFirstChildElement(value);
                if (childElement != null) {
                    textValue = DOMUtils.getTextContent(childElement);
                } else {
                    textValue = DOMUtils.getTextContent(value);
                }
            }
            // if it is not a simple type, skip it
            if (textValue != null) {
                l.add(new NameValuePair(e.getKey(), textValue));
            }
        }
        return EncodingUtil.formUrlEncode(l.toArray(new NameValuePair[0]), "UTF-8");
    }

}
