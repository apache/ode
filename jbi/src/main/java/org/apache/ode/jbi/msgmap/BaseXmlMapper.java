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

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xerces.internal.util.DOMUtil;

public abstract class BaseXmlMapper {
    protected Log __log = LogFactory.getLog(getClass());

    private TransformerFactory _transformerFactory;

    /** Cache of the parsed messages. */
    private static Map<Source, Document> __parsed = Collections.synchronizedMap(new WeakHashMap<Source, Document>());

    private static ThreadLocal<Transformer> __txers = new ThreadLocal();

    protected BaseXmlMapper() {
        _transformerFactory = TransformerFactory.newInstance();
    }

    protected Element parse(Source content) throws MessageTranslationException {
        
        // Check for the message in the cache. note that we are using a synchronized map here,
        // so that we are thread safe, although it is possible for the parse to happen twice. 
        Document parsed = __parsed.get(content);
        if (parsed != null)
            return parsed.getDocumentElement();

        Transformer txer = __txers.get();
        if (txer == null) {
        try {
            txer = _transformerFactory.newTransformer();
                __txers.set(txer);
        } catch (TransformerConfigurationException e) {
            String errmsg = "Transformer configuration error!";
            __log.fatal(errmsg, e);
            throw new Error(errmsg, e);
        }
        }

        try {
            DOMResult domresult = new DOMResult();
            txer.transform(content, domresult);
            parsed = (Document) domresult.getNode();
            // Again, synchronized map... 
            __parsed.put(content, parsed);
            return parsed.getDocumentElement();
        } catch (TransformerException e) {
            throw new MessageTranslationException("Transformer error!", e);
        }
    }

    protected Document newDocument() {
        return DOMUtils.newDocument();
    }

}
