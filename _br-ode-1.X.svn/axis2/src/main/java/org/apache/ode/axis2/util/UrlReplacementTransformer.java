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

import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This encoder applies urlReplacement as defined by the <a href='http://www.w3.org/TR/wsdl#_http:urlReplacement'>WSDL specification</a>.
 * <br/>Surrounding characters for parts may be parentheses '()' or braces '{}'. Pattern with parentheses is look up first, if found then it's replaced with the part value, else the pattern with braces is look up.
 * <p/><strong>Escaping Considerations</strong>
 * <br/>Replacement and default values are escaped. All characters except unreserved (as defined by <a href="http://tools.ietf.org/html/rfc2396#appendix-A">rfc2396</a>) are escaped.
 * <br/> unreserved    = alphanum | mark
 * <br/> mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |  "(" | ")"
 * <p/>
 * <a href="http://tools.ietf.org/html/rfc2396">Rfc2396</a> is used to be compliant with {@linkplain java.net.URI java.net.URI}.
 * <p/>
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 */
public class UrlReplacementTransformer {

    private static final Log log = LogFactory.getLog(UrlReplacementTransformer.class);

    private static final org.apache.ode.axis2.httpbinding.Messages httpMsgs = org.apache.ode.axis2.httpbinding.Messages.getMessages(org.apache.ode.axis2.httpbinding.Messages.class);

    public UrlReplacementTransformer() {
    }

    /**
     * @param baseUri - the base uri template containing part names enclosed within single curly braces
     * @param values  - a map<String, Element>, the key is a part name (without curly braces), the value the replacement value for the part name. If the value is not a simple type, it will be skipped.
     * @return the encoded uri
     * @throws java.lang.IllegalArgumentException
     *          if a replacement value is null in the map or if a part pattern is found more than once
     */
    public String transform(String baseUri, Map<String, Element> values) {
        // the list containing the final split result
        List<String> result = new ArrayList<String>();

        // initial value
        result.add(baseUri);

        // replace each part exactly once
        for (Map.Entry<String, Element> e : values.entrySet()) {

            String partName = e.getKey();
            String replacementValue;
            {
                Element value = e.getValue();
                if (DOMUtils.isEmptyElement(value)) {
                    replacementValue = "";
                } else {
                    /*
                    The expected part value could be a simple type
                    or an element of a simple type.
                    So if a element is there, take its text content
                    else take the text content of the part element itself
                    */
                    Element childElement = DOMUtils.getFirstChildElement(value);
                    if (childElement != null) {
                        replacementValue = DOMUtils.getTextContent(childElement);
                    } else {
                        replacementValue = DOMUtils.getTextContent(value);
                    }
                }
            }

            // if it is not a simple type, skip it
            if (replacementValue != null) {
                try {
                    replacementValue = URIUtil.encodeWithinQuery(replacementValue);
                } catch (URIException urie) {
                    // this exception is never thrown by the code of httpclient
                    if (log.isWarnEnabled()) log.warn(urie.getMessage(), urie);
                }

                // first, search for parentheses
                String partPattern = "\\(" + partName + "\\)";
                if (!replace(result, partPattern, replacementValue)) {
                    // if parentheses not found, try braces
                    partPattern = "\\{" + partName + "\\}";
                    replace(result, partPattern, replacementValue);
                }
            }
        }

        // join all the array elements to form the final url
        StringBuilder sb = new StringBuilder(128);
        for (String aResult : result) sb.append(aResult);
        return sb.toString();
    }

    private boolean replace(List<String> result, String partPattern, String replacementValue) {
        // !!!  i=i+2      replacement values will be skipped,
        // so replaced values do not trigger additional matches
        for (int i = 0; i < result.size(); i = i + 2) {
            String segment = result.get(i);
            // use a negative limit, so empty strings are not discarded
            String[] matches = segment.split(partPattern, -1);
            if (matches.length == 2) {
                // if exactly one match...

                // remove the matching segment
                result.remove(i);
                // replace it with the replacement value
                result.add(i, matches[0]);
                result.add(i + 1, replacementValue);
                result.add(i + 2, matches[1]);

                // pattern found and replaced, we're done for this pattern
                // move on to the next part
                return true;
            }
        }
        return false;
    }
}
