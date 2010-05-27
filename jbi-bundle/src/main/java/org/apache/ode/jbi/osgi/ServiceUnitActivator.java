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

package org.apache.ode.jbi.osgi;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import javax.jbi.component.Component;
import javax.jbi.component.ServiceUnitManager;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author mproch
 *
 */
public class ServiceUnitActivator implements BundleActivator {

    File rootDir;
    String generatedName;

    public void start(BundleContext context) throws Exception {
        generatedName = context.getBundle().getSymbolicName();
        rootDir = context.getDataFile("bpelData");
        rootDir.mkdirs();
        Enumeration<?> en = context.getBundle().findEntries("/", "*", false);
        while (en.hasMoreElements()) {
            copyOne(rootDir, (URL) en.nextElement());
        }
        ServiceReference[] refs = context.getAllServiceReferences(
                "javax.jbi.component.Component", "(&(NAME=OdeBpelEngine))");
        if (refs == null || refs.length != 1) {
            throw new RuntimeException("no appropriate service :(");
        }
        ServiceUnitManager suM = ((Component) context.getService(refs[0]))
                .getServiceUnitManager();
        ClassLoader l = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader suL = suM.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(new BundleClassLoader(suL, context.getBundle()));
            suM.deploy(generatedName, rootDir.getAbsolutePath());
            suM.init(generatedName, rootDir.getAbsolutePath());
            suM.start(generatedName);
        } finally {
            Thread.currentThread().setContextClassLoader(l);
        }

    }

    private void copyOne(File dest, URL url) throws Exception {
        File d = new File(dest, url.getPath());
        InputStream str = url.openStream();
        if (str != null) {
            FileWriter wr = new FileWriter(d);
            try {
                IOUtils.copy(str, wr);
            } finally {
                wr.flush();
                wr.close();
            }
        }
    }

    public void stop(BundleContext context) throws Exception {
        ServiceReference[] refs = context.getAllServiceReferences(
                "javax.jbi.component.Component", "(&(NAME=OdeBpelEngine))");
        if (refs == null || refs.length != 1) {
            throw new RuntimeException("no appropriate service :(");
        }
        ServiceUnitManager suM = ((Component) context.getService(refs[0]))
                .getServiceUnitManager();
        suM.shutDown(generatedName);
        suM.undeploy(generatedName, rootDir.getAbsolutePath());

    }

    public class BundleClassLoader extends ClassLoader {
        private final Bundle delegate;

        public BundleClassLoader(ClassLoader parent, Bundle delegate) {
            super(parent);
            this.delegate = delegate;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            try {
                return getParent().loadClass(name);
            } catch (Exception e) {
                return delegate.loadClass(name);
            }
        }
    }

}
