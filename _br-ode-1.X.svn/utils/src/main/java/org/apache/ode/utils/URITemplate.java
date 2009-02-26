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

package org.apache.ode.utils;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A partial implementation of URI Template expansion
 * as specified by the <a href="http://bitworking.org/projects/URI-Templates/spec/draft-gregorio-uritemplate-03.html">URI template specification</a>.
 * <p/><strong>Limitations</strong>
 * <br/>The only operation implemented so far is <a href="http://bitworking.org/projects/URI-Templates/spec/draft-gregorio-uritemplate-03.html#var">Var substitution</a>. If an expansion template for another operation (join, neg, opt, etc) is found,
 * an {@link UnsupportedOperationException} is thrown.
 * <p/>
 * <p/>
 * <p/><strong>Escaping Considerations</strong>
 * <br/>Replacement and default values are escaped. All characters except unreserved (as defined by <a href="http://tools.ietf.org/html/rfc2396#appendix-A">rfc2396</a>) are escaped.
 * <br/> unreserved    = alphanum | mark
 * <br/> mark          = "-" | "_" | "." | "!" | "~" | "*" | "'" |  "(" | ")"
 * <p/>
 * <a href="http://tools.ietf.org/html/rfc2396">Rfc2396</a> is used to be compliant with {@linkplain java.net.URI java.net.URI}.
 * <p/>
 * <p/><strong>Examples:</strong>
 * <br/>
 * Given the following template variable names and values:
 * <ul>
 * <li>foo = tag</li>
 * <li>bar = java</li>
 * <li>name = null</li>
 * <li>date = 2008/05/09</li>
 * </ul>
 * <p/>The following URI Templates will be expanded as shown:
 * <br/>http://example.com/{foo}/{bar}.{format=xml}
 * <br/>http://example.com/tag/java.xml
 * <br/>
 * <br/>http://example.com/tag/java.{format}
 * <br/>http://example.com/tag/java.
 * <br/>
 * <br/>http://example.com/{foo}/{name}
 * <br/>http://example.com/tag/
 * <br/>
 * <br/>http://example.com/{foo}/{name=james}
 * <br/>http://example.com/tag/james
 * <br/>
 * <br/>http://example.org/{date}
 * <br/>http://example.org/2008%2F05%2F09
 * <br/>
 * <br/>http://example.org/{-join|&|foo,bar,xyzzy,baz}/{date}
 * <br/>--> UnsupportedOperationException
 *
 * @author <a href="mailto:midon@intalio.com">Alexis Midon</a>
 * @see #varSubstitution(String, Object[], java.util.Map)
 */

public class URITemplate {

    private static final Log log = LogFactory.getLog(URITemplate.class);


    public static final String EXPANSION_REGEX = "\\{[^\\}]+\\}";
    // compiled pattern of the regex
    private static final Pattern PATTERN = Pattern.compile(EXPANSION_REGEX);

    /**
     * Implements the function describes in <a href="http://bitworking.org/projects/URI-Templates/spec/draft-gregorio-uritemplate-03.html#appendix_a">the spec</a>
     *
     * @param expansion, an expansion template (with the surrounding braces)
     * @return an array of object containing the operation name, the operation argument, a map of <var, default value (null if none)>
     */
    public static Object[] parseExpansion(String expansion) {
        // remove surrounding braces if any
        if (expansion.matches(EXPANSION_REGEX)) {
            expansion = expansion.substring(1, expansion.length() - 1);
        }
        String[] r;
        if (expansion.contains("|")) {
            // (op, arg, vars)
            r = expansion.split("\\|", -1);
            // remove the leading '-' of the operation
            r[0] = r[0].substring(1);
        } else {
            r = new String[]{null, null, expansion};
        }

        // parse the vars
        Map vars = new HashMap();
        String[] var = r[2].split(",");
        for (String s : var) {
            if (s.contains("=")) {
                String[] a = s.split("=");
                vars.put(a[0], a[1]);
            } else {
                vars.put(s, null);
            }
        }
        // op, arg, vars
        return new Object[]{r[0], r[1], vars};
    }

    /**
     * Simply build a map from nameValuePairs and pass it to {@link #expand(String, java.util.Map)}
     *
     * @param nameValuePairs an array containing of name, value, name, value, and so on.  Null values are allowed.
     * @see # expand (String, java.util.Map)
     */
    public static String expand(String uriTemplate, String... nameValuePairs) throws URIException, UnsupportedOperationException {
        return expand(uriTemplate, toMap(nameValuePairs));
    }

    /**
     * A partial implementation of URI Template expansion
     * as specified by the <a href="http://bitworking.org/projects/URI-Templates/spec/draft-gregorio-uritemplate-03.html">URI template specification</a>.
     * <p/>
     * The only operation implemented as of today is "Var Substitution". If an expansion template for another operation (join, neg, opt, etc) is found,
     * an {@link UnsupportedOperationException} will be thrown.
     * <p/>
     * See {@link #varSubstitution(String, Object[], java.util.Map)}
     *
     * @param uriTemplate    the URI template
     * @param nameValuePairs a Map of &lt;name, value&gt;. Null values are allowed.
     * @return a copy of uri template in which substitutions have been made (if possible)
     * @throws URIException                  if the default protocol charset is not supported
     * @throws UnsupportedOperationException if the operation is not supported. Currently only var substitution is supported.
     * @see #varSubstitution(String, Object[], java.util.Map)
     */
    public static String expand(String uriTemplate, Map<String, String> nameValuePairs) throws URIException, UnsupportedOperationException {
        return expand(uriTemplate, nameValuePairs, false);
    }

    /**
     * Same as {@link #expand(String, java.util.Map)} but preserve an expansion template if the corresponding variable
     * is not defined in the {@code nameValuePairs} map (i.e. map.contains(var)==false).
     * <br/>Meaning that a template may be returned.
     * <br/> If a default value exists for the undefined value, it will be used to replace the expansion pattern.
     * <p/>
     * <strong>Beware that this behavior deviates from the URI Template specification.</strong>
     * <p/>
     * For instance:
     * <br/>Given the following template variable names and values:
     * <ul>
     * <li>bar = java</li>
     * <li>foo undefined
     * </ul>
     * <p/>The following expansion templates will be expanded as shown if {@code preserveUndefinedVar} is true:
     * <br/>http://example.com/{bar}
     * <br/>http://example.com/java
     * <br/>
     * <br/>{foo=a_default_value}
     * <br/>a_default_value
     * <br/>
     * <br/>http://example.com/{bar}/{foo}
     * <br/>http://example.com/java/{foo}
     *
     * @see #expand(String, java.util.Map)
     */
    public static String expandLazily(String uriTemplate, Map<String, String> nameValuePairs) throws URIException, UnsupportedOperationException {
        return expand(uriTemplate, nameValuePairs, true);
    }

    /**
     * @see #expandLazily(String, java.util.Map)
     */
    public static String expandLazily(String uriTemplate, String... nameValuePairs) throws URIException {
        return expandLazily(uriTemplate, toMap(nameValuePairs));
    }


    /**
     * @see #varSubstitution(String, Object[], java.util.Map, boolean)
     * @see #expandLazily(String, String[])
     */
    private static String expand(String uriTemplate, Map<String, String> nameValuePairs, boolean preserveUndefinedVar) throws URIException, UnsupportedOperationException {
        Matcher m = PATTERN.matcher(uriTemplate);
        // Strings are immutable in java
        // so let's use a buffer, and append all substrings between 2 matches and the replacement value for each match 
        StringBuilder sb = new StringBuilder(uriTemplate.length());
        int prevEnd = 0;
        while (m.find()) {
            // append the string between two matches
            sb.append(uriTemplate.substring(prevEnd, m.start()));
            prevEnd = m.end();

            // expansion pattern with braces
            String expansionPattern = uriTemplate.substring(m.start(), m.end());
            Object[] expansionInfo = parseExpansion(expansionPattern);
            String operationName = (String) expansionInfo[0];
            // here we have to know which operation apply
            if (operationName != null) {
                final String msg = "Operation not supported [" + operationName + "]. This expansion pattern [" + expansionPattern + "] is not valid.";
                if (log.isWarnEnabled()) log.warn(msg);
                throw new UnsupportedOperationException(msg);
            } else {
                // here we care only for var substitution, i.e expansion patterns with no operation name
                sb.append(varSubstitution(expansionPattern, expansionInfo, nameValuePairs, preserveUndefinedVar));
            }

        }
        if (sb.length() == 0) {
            // return the template itself if no match (String are immutable in java, no need to clone the template)
            return uriTemplate;
        } else {
            // don't forget the remaining part
            sb.append(uriTemplate.substring(prevEnd, uriTemplate.length()));
            return sb.toString();
        }
    }

    /**
     * An implementation of var substitution as defined by the
     * <a href="http://bitworking.org/projects/URI-Templates/spec/draft-gregorio-uritemplate-03.html#var">URI template specification</a>.
     * <p/>
     * If for a given variable, the variable is in the name/value map but the associated value is null. The variable will be replaced with an empty string or with the default value if any.
     *
     * @param expansionPattern an expansion pattern (not a uri template) e.g. "{foo}"
     * @param expansionInfo    the result of {@link #parseExpansion(String)} for the given expansion pattern
     * @param nameValuePairs   the Map<String, String> of names and associated values. May containt null values.
     * @return the expanded string, properly escaped.
     * @throws URIException if an encoding exception occured
     * @see org.apache.commons.httpclient.util.URIUtil#encodeWithinQuery(String)
     * @see java.net.URI
     */
    public static String varSubstitution(String expansionPattern, Object[] expansionInfo, Map<String, String> nameValuePairs) throws URIException {
        return varSubstitution(expansionPattern, expansionInfo, nameValuePairs, false);
    }

    /**
     * Same as {@link #varSubstitution(String, Object[], java.util.Map)} but the {@code preserveUndefinedVar} boolean
     * argument (if {@code true}) allows to preserve an expansion template if the corresponding variable is not defined in the {@code nameValuePairs} map (i.e. map.contains(var)==false).
     * <br/> If a default value exists for the undefined value, it will be used to replace the expansion pattern.
     * <p/>
     * <strong>Beware that this behavior deviates from the URI Template specification.</strong>
     * <p/>
     * For instance:
     * <br/>Given the following template variable names and values:
     * <ul>
     * <li>bar = java</li>
     * <li>foo undefined
     * </ul>
     * <p/>The following expansion templates will be expanded as shown if {@code preserveUndefinedVar} is true:
     * <br/>{bar}
     * <br/>java
     * <br/>
     * <br/>{foo=a_default_value}
     * <br/>a_default_value
     * <br/>
     * <br/>{foo}
     * <br/>{foo}
     */
    public static String varSubstitution(String expansionPattern, Object[] expansionInfo, Map<String, String> nameValuePairs, boolean preserveUndefinedVar) throws URIException {
        Map vars = (Map) expansionInfo[2];
        // only one var per pattern
        Map.Entry e = (Map.Entry) vars.entrySet().iterator().next();
        String var = (String) e.getKey();
        String defaultValue = (String) e.getValue();
        boolean hasDefaultValue = defaultValue != null;
        // this boolean indicates if the var is mentioned in the map, not that the associated value is not null.
        boolean varDefined = nameValuePairs.containsKey(var);
        String providedValue = nameValuePairs.get(var);
        String res;
        boolean escapingNeeded = true;
        if (varDefined) {
            if (providedValue == null && !hasDefaultValue) {
                res = "";
            } else {
                res = providedValue != null ? providedValue : defaultValue;
            }
        } else {
            // If the variable is undefined and no default value is given then substitute with the empty string,
            // except if preserveUndefinedVar is true

            if (hasDefaultValue) {
                res = defaultValue;
            } else {
                if (preserveUndefinedVar) {
                    res = expansionPattern;
                    escapingNeeded = false;
                } else {
                    res = "";
                }
            }
        }
        // We assume that the replacement value is for the query part of the URI.
        // Actually the query allows less character than the path part. $%&+,:@
        // (acording to RFC2396
        return escapingNeeded ? URIUtil.encodeWithinQuery(res) : res;
    }


    private static Map<String, String> toMap(String... nameValuePairs) {
        if (nameValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("An even number of elements is expected.");
        }
        Map<String, String> m = new HashMap<String, String>();
        for (int i = 0; i < nameValuePairs.length; i = i + 2) {
            m.put(nameValuePairs[i], nameValuePairs[i + 1]);
        }
        return m;
    }
}
