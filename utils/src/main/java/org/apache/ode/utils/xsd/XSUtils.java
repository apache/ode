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

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.ode.utils.msg.MessageBundle;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.ls.LSInput;


/**
 * Various utility methods related to XML Schema processing.
 */
public class XSUtils {

    private static final Logger __log = LoggerFactory.getLogger(XSUtils.class);

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
                                                 XMLEntityResolver resolver, int localSchemaId) throws XsdException {
        if (__log.isDebugEnabled())
            __log.debug("captureSchema(URI,Text,...): systemURI=" + systemURI);

        DOMInputImpl input = new DOMInputImpl();
        input.setSystemId(systemURI.toString());
        input.setByteStream(new ByteArrayInputStream(schemaData));

        Map<URI, byte[]> ret = captureSchema(input, resolver);
        
        URI localURI = localSchemaId == 0 ? systemURI : URI.create(systemURI.toString() + '.' + localSchemaId);
        ret.put(localURI, schemaData);
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

        LoggingDOMErrorHandler deh = new LoggingDOMErrorHandler(__log);
        schemaLoader.setParameter(Constants.DOM_ERROR_HANDLER, deh);

        XSModel model = schemaLoader.load(input);

        // The following mess is due to XMLSchemaLoaders funkyness in error
        // reporting: sometimes it throws an exception, sometime it returns
        // null, sometimes it just prints bs to the screen.
        if (model == null) {
            /*
            * Someone inside Xerces will have eaten this exception, for no good
            * reason.
            */
            XsdException ex = null;

            List<XMLParseException> errors = eh.getErrors();
            if (errors.size() != 0) {
                __log.error("captureSchema: XMLParseException(s) in " + input);

                for (XMLParseException xpe : errors) {
                    ex = new XsdException(ex, xpe.getMessage(), xpe.getLineNumber(), xpe.getColumnNumber(),
                            xpe.getLiteralSystemId());
                }
            }

            List<Exception> exceptions = deh.getExceptions();
            if (exceptions.size() != 0) {
                for (Exception e : exceptions) {
                    ex = new XsdException(ex, e.getMessage());
                }
            }

            if (ex != null) {
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

        private Logger _log;

        private ArrayList<XMLParseException> _errors = new ArrayList<XMLParseException>();

        /**
         * Create a new instance that will output to the specified {@link Logger}
         * instance.
         * @param log the target log, which much be non-<code>null</code>
         */
        public LoggingXmlErrorHandler(Logger log) {
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

    static class LoggingDOMErrorHandler implements DOMErrorHandler {

        private ArrayList<Exception> _exceptions = new ArrayList<Exception>();
        private Logger _log;

        public LoggingDOMErrorHandler(Logger log) {
            assert log != null;
            _log = log;
        }

        public boolean handleError(DOMError error) {
            if (_log.isDebugEnabled()) {
                _log.debug("Exception occurred during parsing schema: " + error.getMessage());
            }
            if (error != null) {
                _exceptions.add((Exception) error.getRelatedException());
            }
            return false;
        }

        public ArrayList<Exception> getExceptions() {
            return _exceptions;
        }
    }
}
