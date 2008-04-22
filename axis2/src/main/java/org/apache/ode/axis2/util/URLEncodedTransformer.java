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
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.axis2.httpbinding.Messages;
import org.w3c.dom.Element;

import javax.wsdl.Part;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class URLEncodedTransformer {

    private static final Messages msgs = Messages.getMessages(Messages.class);

    public String transform(Map<String, Element> values) {
        if (values.isEmpty()) return null;
        NameValuePair[] pairs = new NameValuePair[values.size()];
        int i = 0;
        for (Map.Entry<String, Element> e : values.entrySet()) {
            Element node = e.getValue();
            String nodeContent = DOMUtils.isEmptyElement(node) ? "" : DOMUtils.getTextContent(node);
            if (nodeContent == null) {
                throw new IllegalArgumentException(msgs.msgSimpleTypeExpected(e.getKey()));
            }
            NameValuePair p = new NameValuePair(e.getKey(), nodeContent);
            pairs[i++] = p;
        }
        return EncodingUtil.formUrlEncode(pairs, "UTF-8");
    }

}
