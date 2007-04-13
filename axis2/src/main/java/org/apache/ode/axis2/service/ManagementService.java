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

package org.apache.ode.axis2.service;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.receivers.AbstractMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.OdeFault;
import org.apache.ode.axis2.hooks.ODEAxisService;
import org.apache.ode.axis2.util.OMUtils;
import org.apache.ode.bpel.engine.ProcessAndInstanceManagementImpl;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.pmapi.InstanceManagement;
import org.apache.ode.bpel.pmapi.ProcessInfoCustomizer;
import org.apache.ode.bpel.pmapi.ProcessManagement;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Node;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Axis2 wrapper for process and instance management interfaces.
 */
public class ManagementService {

    private static final Log __log = LogFactory.getLog(ManagementService.class);

    private ProcessManagement _processMgmt;
    private InstanceManagement _instanceMgmt;

    public void enableService(AxisConfiguration axisConfig, BpelServer server, ProcessStore _store, String rootpath) {
        ProcessAndInstanceManagementImpl pm = new ProcessAndInstanceManagementImpl(server,_store);
        _processMgmt = pm;
        _instanceMgmt = pm;

        Definition def;
        try {
            WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
            wsdlReader.setFeature("javax.wsdl.verbose", false);

            def = wsdlReader.readWSDL(rootpath + "/pmapi.wsdl");
            AxisService processService = ODEAxisService.createService(
                    axisConfig, new QName("http://www.apache.org/ode/pmapi", "ProcessManagementService"),
                    "ProcessManagementPort", "ProcessManagement", def, new ProcessMessageReceiver());
            AxisService instanceService = ODEAxisService.createService(
                    axisConfig, new QName("http://www.apache.org/ode/pmapi", "InstanceManagementService"),
                    "InstanceManagementPort", "InstanceManagement", def, new InstanceMessageReceiver());
            axisConfig.addService(processService);
            axisConfig.addService(instanceService);
        } catch (WSDLException e) {
            __log.error("Couldn't start-up management services!", e);
        } catch (IOException e) {
            __log.error("Couldn't start-up management services!", e);
        }
    }

    private static void receive(MessageContext msgContext, Class mgmtClass,
                                Object mgmtObject, SOAPFactory soapFactory) throws AxisFault {
        if (__log.isDebugEnabled())
            __log.debug("Received mgmt message for " + msgContext.getAxisService().getName() +
                    "." + msgContext.getAxisOperation().getName());

        String methodName = msgContext.getAxisOperation().getName().getLocalPart();
        try {
            // Our services are defined in WSDL which requires operation names to be different
            Method invokedMethod = findMethod(mgmtClass, methodName);
            Object[] params = extractParams(invokedMethod, msgContext.getEnvelope().getBody().getFirstElement());
            Object result = invokedMethod.invoke(mgmtObject, params);

            if (hasResponse(msgContext.getAxisOperation())) {
                MessageContext outMsgContext = Utils.createOutMessageContext(msgContext);
                outMsgContext.getOperationContext().addMessageContext(outMsgContext);

                SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
                outMsgContext.setEnvelope(envelope);

                envelope.getBody().addChild(convertToOM(soapFactory, result));

                if (__log.isDebugEnabled()) {
                    __log.debug("Reply mgmt for " + msgContext.getAxisService().getName() +
                            "." + msgContext.getAxisOperation().getName());
                    __log.debug("Reply mgmt message " + outMsgContext.getEnvelope());
                }
                AxisEngine engine = new AxisEngine(
                        msgContext.getOperationContext().getServiceContext().getConfigurationContext());
                engine.send(outMsgContext);
            }
        } catch (IllegalAccessException e) {
            throw new OdeFault("Couldn't invoke method named " + methodName + " in management interface!", e);
        } catch (InvocationTargetException e) {
            throw new OdeFault("Invocation of method " + methodName + " in management interface failed!", e);
        }
    }

    private static Object[] extractParams(Method method, OMElement omElmt) throws AxisFault {
        Class[] paramTypes = method.getParameterTypes();
        Object[] params = new Object[method.getParameterTypes().length];
        Iterator omChildren = omElmt.getChildElements();
        int paramIdx = 0;
        for (Class<?> paramClass : paramTypes) {
            OMElement omchild = (OMElement) omChildren.next();
            __log.debug("Extracting param " + paramClass + " from " + omchild);
            params[paramIdx++] = convertFromOM(paramClass, omchild);
        }
        return params;
    }

    private static Object convertFromOM(Class clazz, OMElement elmt) throws AxisFault {
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
            Iterator children = elmt.getChildElements();
            Class targetClazz = clazz.getComponentType();
            while (children.hasNext())
                alist.add(parseType(targetClazz, ((OMElement)children.next()).getText()));
            return alist.toArray((Object[]) Array.newInstance(targetClazz, alist.size()));
        } else if (XmlObject.class.isAssignableFrom(clazz)) {
            try {
                Class beanFactory = Class.forName(clazz.getCanonicalName() + ".Factory");
                return beanFactory.getMethod("parse", XMLStreamReader.class)
                        .invoke(elmt.getXMLStreamReaderWithoutCaching());
            } catch (ClassNotFoundException e) {
                throw new OdeFault("Couldn't find class " + clazz.getCanonicalName() + ".Factory to instantiate xml bean", e);
            } catch (IllegalAccessException e) {
                throw new OdeFault("Couldn't access class " + clazz.getCanonicalName() + ".Factory to instantiate xml bean", e);
            } catch (InvocationTargetException e) {
                throw new OdeFault("Couldn't access xml bean parse method on class " + clazz.getCanonicalName() + ".Factory " +
                        "to instantiate xml bean", e);
            } catch (NoSuchMethodException e) {
                throw new OdeFault("Couldn't find xml bean parse method on class " + clazz.getCanonicalName() + ".Factory " +
                        "to instantiate xml bean", e);
            }
        } else throw new OdeFault("Couldn't use element " + elmt + " to obtain a management method parameter.");
    }

    private static OMElement convertToOM(SOAPFactory soapFactory, Object obj) throws AxisFault {
        if (obj instanceof XmlObject) {
            try {
                return new StAXOMBuilder(((XmlObject)obj).newInputStream()).getDocumentElement();
            } catch (XMLStreamException e) {
                throw new OdeFault("Couldn't serialize result to an outgoing messages.", e);
            }
        } else if (obj instanceof List) {
            OMElement listElmt = soapFactory.createOMElement("list", null);
            for (Object stuff : ((List) obj)) {
                OMElement stuffElmt = soapFactory.createOMElement("element", null);
                stuffElmt.setText(stuff.toString());
                listElmt.addChild(stuffElmt);
            }
            return listElmt;
        } else throw new OdeFault("Couldn't convert object " + obj + " into a response element.");
    }

    private static boolean hasResponse(AxisOperation op) {
        switch(op.getAxisSpecifMEPConstant()) {
            case AxisOperation.WSDL20_2004Constants.MEP_CONSTANT_IN_OUT: return true;
            case AxisOperation.WSDL20_2004Constants.MEP_CONSTANT_OUT_ONLY: return true;
            case AxisOperation.WSDL20_2004Constants.MEP_CONSTANT_OUT_OPTIONAL_IN: return true;
            case AxisOperation.WSDL20_2004Constants.MEP_CONSTANT_ROBUST_OUT_ONLY: return true;
            default: return false;
        }
    }

    class ProcessMessageReceiver extends AbstractMessageReceiver {
        public void receive(MessageContext messageContext) throws AxisFault {
            ManagementService.receive(messageContext, ProcessManagement.class,
                    _processMgmt, getSOAPFactory(messageContext));
        }
    }

    class InstanceMessageReceiver extends AbstractMessageReceiver {
        public void receive(MessageContext messageContext) throws AxisFault {
            ManagementService.receive(messageContext, InstanceManagement.class,
                    _instanceMgmt, getSOAPFactory(messageContext));
        }
    }

    private static Method findMethod(Class clazz, String methodName) throws AxisFault {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)) return method;
        }
        throw new OdeFault("Couldn't find any method named " + methodName + " in interface " + clazz.getName());
    }

    private static Object parseType(Class clazz, String str) {
        if (clazz.equals(Integer.class)) return Integer.valueOf(str);
        if (clazz.equals(Float.class)) return Integer.valueOf(str);
        if (clazz.equals(String.class)) return str;
        return null;
    }
}
