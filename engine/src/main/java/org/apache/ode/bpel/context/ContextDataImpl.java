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
package org.apache.ode.bpel.context;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ode.bpel.rapi.ContextData;
import org.apache.ode.schemas.context.x2009.ContextsDocument;
import org.apache.ode.schemas.context.x2009.TContext;
import org.apache.ode.schemas.context.x2009.TContextValue;
import org.apache.ode.schemas.context.x2009.TContexts;
import org.apache.xmlbeans.XmlException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Represents a BPEL context.
 * 
 * The context contains key/value pairs. The class basically serves as a
 * data transfer object between interceptors and the engine's core.
 * 
 * @author Tammo van Lessen
 */
public class ContextDataImpl implements ContextData {

    protected Map<String, Map<String,String>> contexts = new LinkedHashMap<String, Map<String, String>>();
    
    public ContextDataImpl() {
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#put(java.lang.String, java.lang.String, java.lang.String)
     */
    public void put(String context, String key, String value) {
        Map<String, String> ctx = contexts.get(context);
        if (ctx == null) {
            ctx = new LinkedHashMap<String, String>();
            contexts.put(context, ctx);
        }
        ctx.put(key, value);
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#put(org.apache.ode.bpel.context.ContextName, java.lang.String)
     */
    public void put(ContextName name, String value) {
    	put(name.getNamespace(), name.getKey(), value);
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#get(java.lang.String, java.lang.String)
     */
    public String get(String context, String key) {
        Map<String, String> ctx = contexts.get(context);
        if (ctx != null) {
            return ctx.get(key);
        }
        
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#get(org.apache.ode.bpel.context.ContextName)
     */
    public String get(ContextName name) {
    	return get(name.getNamespace(), name.getKey());
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#isSet(java.lang.String, java.lang.String)
     */
    public boolean isSet(String context, String key) {
        Map<String, String> ctx = contexts.get(context);
        if (ctx != null) {
            return ctx.get(key) != null;
        }
        
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#isSet(org.apache.ode.bpel.context.ContextName)
     */
    public boolean isSet(ContextName name) {
    	return isSet(name.getNamespace(), name.getKey());
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#getKeys(java.lang.String)
     */
    public String[] getKeys(String context) {
        Map<String, String> ctx = contexts.get(context);
        if (ctx != null) {
            return ctx.keySet().toArray(new String[] {});
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#getContexts()
     */
    public String[] getContexts() {
        return contexts.keySet().toArray(new String[] {});
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#getContextNames()
     */
    public List<ContextName> getContextNames() {
    	List<ContextName> names = new LinkedList<ContextName>();
    	for (String context : contexts.keySet()) {
    		for (String key : contexts.get(context).keySet()) {
    			names.add(new ContextName(context, key));
    		}
    	}
    	
    	return Collections.unmodifiableList(names);
    }
    
    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#removeContext(java.lang.String)
     */
    public void removeContext(String name) {
        contexts.remove(name);
    }

    /* (non-Javadoc)
     * @see org.apache.ode.bpel.context.ContextData#toXML()
     */
    public Element toXML(Set<String> contextFilter) {
        if (contextFilter == null) {
            contextFilter = new HashSet<String>();
            contextFilter.add("*");
        }
        
        ContextsDocument doc = ContextsDocument.Factory.newInstance();
        TContexts ctxs = doc.addNewContexts();
        for (String cname : contexts.keySet()) {
            if (!contextFilter.contains("*") && !contextFilter.contains(cname)) {
                continue;
            }
            TContext ctx = ctxs.addNewContext();
            ctx.setName(cname);
            Map<String, String> cvalues = contexts.get(cname); 
            for (String key : cvalues.keySet()) {
                TContextValue ctxval = ctx.addNewValue();
                ctxval.setKey(key);
                ctxval.setStringValue(cvalues.get(key));
            }
        }
        
        // workaround to a level 3 dom node
        Element root = ((Document)doc.newDomNode()).getDocumentElement();
        
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document level3doc = builder.newDocument();
            level3doc.appendChild(level3doc.importNode(root, true));
            return level3doc.getDocumentElement();

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Element toXML() {
        return toXML(null);
    }
    
    public static ContextData fromXML(Node node) {
        try {
            TContexts ctxs = ContextsDocument.Factory.parse(node).getContexts();
            if (ctxs == null) {
                return null;
            }
            ContextData cdata = new ContextDataImpl();
            for (TContext ctx : ctxs.getContextList()) {
                for (TContextValue ctxval : ctx.getValueList()) {
                    cdata.put(ctx.getName(), ctxval.getKey(), ctxval.getStringValue());
                }
            }
            
            return cdata;
        } catch (XmlException e) {
            throw new RuntimeException(e);
        }
    }
    
 }
