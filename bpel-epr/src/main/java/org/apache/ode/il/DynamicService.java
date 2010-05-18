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

package org.apache.ode.il;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.NamespaceImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.pmapi.ProcessInfoCustomizer;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

/**
 * Dynamic invocation handler for XML-based service;  uses RPC message format conventions to dispatch
 * invocation based on top-level element name (e.g. method name) and sub-elements (e.g. parameters).
 */
public class DynamicService<T> {
    private final Log __log = LogFactory.getLog(getClass());

    static final OMFactory OM = OMAbstractFactory.getOMFactory();

    T _service;
    Class<T> _clazz;
    
    @SuppressWarnings("unchecked")
    public DynamicService(T service) {
        _clazz = (Class<T>) service.getClass();
        _service = service;
    }
    
    public OMElement invoke(String operation, OMElement payload) {
        if (__log.isDebugEnabled())
            __log.debug("Invoke: operation "+operation+" on "+_clazz + ":\n" + payload);

        String methodName = operation;
        try {
            Method invokedMethod = findMethod(methodName);
            Object[] params = extractParams(invokedMethod, payload);
            Object result = invokedMethod.invoke(_service, params);
            OMElement response = null;
            if (result != null) {
                if (__log.isDebugEnabled())
                    __log.debug("Invoke: operation "+operation+" on "+_clazz + ":\n" + payload + "\nOM:" + OM + " namespace:" + payload.getNamespace());
                response = OM.createOMElement(new QName((payload.getNamespace() == null ? "" : payload.getNamespace().getNamespaceURI()), methodName+"Response"));

                OMElement parts = convertToOM(result);
                parts = stripNamespace(parts);
                response.addChild(parts);
            }
            if (__log.isDebugEnabled()) {
                __log.debug("Response: operation "+operation+" on "+_clazz + ":\n" + response);
            }
            return response;
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Couldn't invoke method named " + methodName + " in management interface!", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Invocation of method " + methodName + " in management interface failed: " + e.getTargetException().getMessage(), e.getTargetException());
        }
    }

    @SuppressWarnings("unchecked")
    private Object[] extractParams(Method method, OMElement omElmt) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[method.getParameterTypes().length];
        Iterator<OMElement> omChildren = (Iterator<OMElement>) omElmt.getChildElements();
        int paramIdx = 0;
        for (Class<?> paramClass : paramTypes) {
            OMElement omchild = (OMElement) omChildren.next();
            __log.debug("Extracting param " + paramClass + " from " + omchild);
            params[paramIdx++] = convertFromOM(paramClass, omchild);
        }
        return params;
    }

    @SuppressWarnings("unchecked")
    private static Object convertFromOM(Class<?> clazz, OMElement elmt) {
        // Here comes the nasty code...
        if (elmt == null || elmt.getText().length() == 0 && !elmt.getChildElements().hasNext())
            return null;
        else if (clazz.equals(String.class)) {
            return elmt.getText();
        } else if (clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE)) {
            return (elmt.getText().equals("true") || elmt.getText().equals("yes")) ? Boolean.TRUE : Boolean.FALSE;
        } else if (clazz.equals(QName.class)) {
            // The getTextAsQName is buggy, it sometimes return the full text without extracting namespace
            return OMUtils.getTextAsQName(elmt);
        } else if (clazz.equals(ProcessInfoCustomizer.class)) {
            return new ProcessInfoCustomizer(elmt.getText());
        } else if (Node.class.isAssignableFrom(clazz)) {
            return OMUtils.toDOM(elmt.getFirstElement());
        } else if (clazz.equals(Long.TYPE) || clazz.equals(Long.class)) {
            return Long.parseLong(elmt.getText());
        } else if (clazz.equals(Integer.TYPE) || clazz.equals(Integer.class)) {
            return Integer.parseInt(elmt.getText());
        } else if (clazz.isArray()) {
            ArrayList<Object> alist = new ArrayList<Object>();
            Iterator<OMElement> children = elmt.getChildElements();
            Class<?> targetClazz = clazz.getComponentType();
            while (children.hasNext())
                alist.add(parseType(targetClazz, ((OMElement)children.next()).getText()));
            return alist.toArray((Object[]) Array.newInstance(targetClazz, alist.size()));
        } else if (XmlObject.class.isAssignableFrom(clazz)) {
            try {
                Class beanFactory = clazz.forName(clazz.getCanonicalName() + "$Factory");
                elmt.setNamespace(new NamespaceImpl(""));
                elmt.setLocalName("xml-fragment");
                return beanFactory.getMethod("parse", XMLStreamReader.class)
                        .invoke(null, elmt.getXMLStreamReaderWithoutCaching());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Couldn't find class " + clazz.getCanonicalName() + ".Factory to instantiate xml bean", e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Couldn't access class " + clazz.getCanonicalName() + ".Factory to instantiate xml bean", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Couldn't access xml bean parse method on class " + clazz.getCanonicalName() + ".Factory " +
                        "to instantiate xml bean", e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Couldn't find xml bean parse method on class " + clazz.getCanonicalName() + ".Factory " +
                        "to instantiate xml bean", e);
            }
        } else throw new RuntimeException("Couldn't use element " + elmt + " to obtain a management method parameter.");
    }

    @SuppressWarnings("unchecked")
    private static OMElement convertToOM(Object obj) {
        if (obj instanceof XmlObject) {
            try {
                return new StAXOMBuilder(((XmlObject)obj).newInputStream()).getDocumentElement();
            } catch (XMLStreamException e) {
                throw new RuntimeException("Couldn't serialize result to an outgoing messages.", e);
            }
        } else if (obj instanceof List) {
            OMElement listElmt = OM.createOMElement("list", null);
            for (Object stuff : ((List) obj)) {
                OMElement stuffElmt = OM.createOMElement("element", null);
                stuffElmt.setText(stuff.toString());
                listElmt.addChild(stuffElmt);
            }
            return listElmt;
        } else throw new RuntimeException("Couldn't convert object " + obj + " into a response element.");
    }

    @SuppressWarnings("unchecked")
    private static OMElement stripNamespace(OMElement element) {
        OMElement parent = OM.createOMElement(new QName("", element.getLocalName()));
        Iterator<OMElement> iter = (Iterator<OMElement>) element.getChildElements();
        while (iter.hasNext()) {
            OMElement child = iter.next();
            child = child.cloneOMElement();
            parent.addChild(child);
        }
        return parent;
    }

    private Method findMethod(String methodName) {
        for (Method method : _clazz.getMethods()) {
            if (method.getName().equals(methodName)) return method;
        }
        throw new RuntimeException("Couldn't find any method named " + methodName + " in interface " + _clazz.getName());
    }

    private static Object parseType(Class<?> clazz, String str) {
        if (clazz.equals(Integer.class)) return Integer.valueOf(str);
        if (clazz.equals(Float.class)) return Integer.valueOf(str);
        if (clazz.equals(String.class)) return str;
        return null;
    }
    
}
