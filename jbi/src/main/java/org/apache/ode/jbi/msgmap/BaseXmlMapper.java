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

package org.apache.ode.jbi.msgmap;

import java.util.WeakHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public abstract class BaseXmlMapper {
    protected Log __log = LogFactory.getLog(getClass());

    private DocumentBuilderFactory _dbf;

    private TransformerFactory _transformerFactory;

    /** Cache of the parsed messages. */
    private static WeakHashMap<Source, Document> __parsed = new WeakHashMap<Source, Document>();

    protected BaseXmlMapper() {
        _transformerFactory = TransformerFactory.newInstance();
        _dbf = DocumentBuilderFactory.newInstance();
    }

    protected Element parse(Source content) throws MessageTranslationException {
        Document parsed = __parsed.get(content);
        if (parsed != null)
            return parsed.getDocumentElement();

        Transformer txer = null;
        try {
            txer = _transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            String errmsg = "Transformer configuration error!";
            __log.fatal(errmsg, e);
            throw new Error(errmsg, e);
        }

        try {
            DOMResult domresult = new DOMResult();
            txer.transform(content, domresult);
            parsed = (Document) domresult.getNode();
            __parsed.put(content, parsed);
            return parsed.getDocumentElement();
        } catch (TransformerException e) {
            throw new MessageTranslationException("Transformer error!", e);
        }
    }

    protected Document newDocument() {
        try {
            return _dbf.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            String errmsg = "Parser configuration error!";
            __log.fatal(errmsg, e);
            throw new Error(errmsg, e);
        }
    }

}
