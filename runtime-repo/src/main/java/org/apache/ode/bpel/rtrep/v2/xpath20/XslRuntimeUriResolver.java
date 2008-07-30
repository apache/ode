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

package org.apache.ode.bpel.elang.xpath20.runtime;

import org.apache.ode.bpel.compiler.api.CompilationException;
import org.apache.ode.bpel.elang.xpath10.o.OXPath10Expression;
import org.apache.ode.bpel.o.OXslSheet;
import org.apache.ode.utils.StreamUtils;
import org.apache.ode.utils.fs.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

/**
 * Used to give the Xsl processor a way to access included XSL sheets
 * by using the maps of sheets pre-processed at compilation time and
 * stored in the OXPath10Expression.
 */
public class XslRuntimeUriResolver implements URIResolver {

    private static final Log __log = LogFactory.getLog(XslRuntimeUriResolver.class);

    private OXPath10Expression _expr;
    private final URI _baseResourceURI;

    public XslRuntimeUriResolver(OXPath10Expression expr, URI baseResourceURI) {
        _expr = expr;
        _baseResourceURI= baseResourceURI;
    }

    public Source resolve(String href, String base) throws TransformerException {
        String result;
        URI uri;
        try {
            uri = new URI(FileUtils.encodePath(href));
        } catch (URISyntaxException e) {
            return null;
        }

        OXslSheet sheet = _expr.xslSheets.get(uri);
        if( sheet != null) {
            result = sheet.sheetBody;
        } else {
            result = getResourceAsString(uri);
        }

        if( result != null ) {
            return new StreamSource(new StringReader(result));
        } else {
            return null;
        }
    }

    /**
     * Given a URI this function will attempt to retrieve the resource declared at that URI location
     * as a string.  (Hopefully everything's character encodings are all ok...)  This URI can be
     * defined as being relative to the executing process instance's physical file location.
     *
     * @param docUri - the URI to resolve
     * @return String - the resource contents, or null if none found.
     */
    private String getResourceAsString(URI docUri) {
        URI resolvedURI= _baseResourceURI.resolve(docUri);
        InputStream is = null;
        try {
            File f = new File(resolvedURI);
            if (!f.exists()) return null;
            is = new FileInputStream(f);
            return new String(StreamUtils.read(is));
        } catch (IOException e) {
            __log.info("Couldn't load XSL resource " + docUri);
        } finally {
            try {
                if (is != null) is.close();
            } catch (Exception ex) {
                // No worries.
            }
        }
        return null;
    }



}
