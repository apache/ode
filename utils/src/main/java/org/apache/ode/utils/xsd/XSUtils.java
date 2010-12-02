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
package org.apache.ode.utils.xsd;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.ls.LSInput;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Various utility methods related to XML Schema processing.
 */
public class XSUtils {

    private static final Log __log = LogFactory.getLog(XSUtils.class);

    private static final XsdMessages __msgs = MessageBundle.getMessages(XsdMessages.class);

    /**
     * Capture the schemas supplied by the reader.  <code>systemURI</code> is
     * required to resolve any relative uris encountered during the parse.
     *
     * @param systemURI Used to resolve relative uris.
     * @param schemaData the top level schema.
     * @param resolver entity resolver
     *
     * @return
     */
    public static Map<URI, byte[]> captureSchema(URI systemURI, byte[] schemaData,
                                                 XMLEntityResolver resolver) throws XsdException {
        if (__log.isDebugEnabled())
            __log.debug("captureSchema(URI,Text,...): systemURI=" + systemURI);

        DOMInputImpl input = new DOMInputImpl();
        input.setSystemId(systemURI.toString());
        input.setByteStream(new ByteArrayInputStream(schemaData));

        Map<URI, byte[]> ret = captureSchema(input, resolver);
        ret.put(systemURI, schemaData);
        return ret;
    }

    private static Map<URI, byte[]> captureSchema(LSInput input, XMLEntityResolver resolver)
            throws XsdException {
        if (__log.isDebugEnabled())
            __log.debug("captureSchema(LSInput,...): input.systemId=" + input.getSystemId());

        Map<URI, byte[]> captured = new HashMap<URI, byte[]>();

        if (resolver == null) {
            throw new IllegalStateException("no resolver set");
        }

        CapturingXMLEntityResolver cr = new CapturingXMLEntityResolver(captured, resolver);

        XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
        schemaLoader.setEntityResolver(cr);
        schemaLoader.setFeature("http://apache.org/xml/features/honour-all-schemaLocations", true);

        LoggingXmlErrorHandler eh = new LoggingXmlErrorHandler(__log);
        schemaLoader.setErrorHandler(eh);

        XSModel model = schemaLoader.load(input);

        // The following mess is due to XMLSchemaLoaders funkyness in error
        // reporting: sometimes it throws an exception, sometime it returns
        // null, sometimes it just prints bs to the screen.
        if (model == null) {
            /*
            * Someone inside Xerces will have eaten this exception, for no good
            * reason.
            */
            List<XMLParseException> errors = eh.getErrors();
            if (errors.size() != 0) {
                __log.error("captureSchema: XMLParseException(s) in " + input);

                XsdException ex = null;
                for (XMLParseException xpe : errors) {
                    ex = new XsdException(ex, xpe.getMessage(), xpe.getLineNumber(), xpe.getColumnNumber(),
                            xpe.getLiteralSystemId());
                }
                throw ex;
            }

            if (__log.isDebugEnabled())
                __log.debug("captureSchema: NULL model (unknown error) for " + input.getSystemId());
        }

        return captured;
    }

    /**
     * Implementation of {@link LoggingXmlErrorHandler} that outputs messages to
     * a log.
     */
    static class LoggingXmlErrorHandler implements XMLErrorHandler {

        private Log _log;

        private ArrayList<XMLParseException> _errors = new ArrayList<XMLParseException>();

        /**
         * Create a new instance that will output to the specified {@link Log}
         * instance.
         * @param log the target log, which much be non-<code>null</code>
         */
        public LoggingXmlErrorHandler(Log log) {
            assert log != null;
            _log = log;
        }

        public List<XMLParseException> getErrors() {
            return _errors;
        }

        /**
         * @see XMLErrorHandler#warning(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
         */
        public void warning(String domain, String key, XMLParseException ex) throws XNIException {
            if (_log.isDebugEnabled())
                _log.debug("XSDErrorHandler.warning: domain=" + domain + ", key=" + key,ex);

            if (ex != null) {
                _errors.add(ex);
                throw ex;
            }
        }

        /**
         * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
         */
        public void error(String domain, String key, XMLParseException ex) throws XNIException {
            if (_log.isDebugEnabled())
                _log.debug("XSDErrorHandler.error: domain=" + domain + ", key=" + key,ex);

            if (ex != null) {
                _errors.add(ex);
                throw ex;
            }

            // Should not reach here, but just in case...
            throw new XNIException("Unknown XSD error state; domain=" + domain + ", key=" +key);
        }

        public void fatalError(String domain, String key, XMLParseException ex) throws XNIException {
            if (_log.isDebugEnabled())
                _log.debug("XSDErrorHandler.fatal: domain=" + domain + ", key=" + key,ex);

            if (ex != null) {
                _errors.add(ex);
                throw ex;
            }

            // Should not reach here, but just in case...
            throw new XNIException("Unknown XSD error state; domain=" + domain + ", key=" +key);
        }
    }
}
