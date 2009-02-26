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

package org.apache.ode.axis2.util;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.MultiParentClassLoader;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.transaction.context.GeronimoTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;

import javax.transaction.TransactionManager;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class GeronimoFactory {

    /* Public no-arg contructor is required */
    public GeronimoFactory() {
    }

    public TransactionManager getTransactionManager() {
        Kernel kernel = KernelRegistry.getSingleKernel();
        TransactionContextManager ctxManager = null;

        try {
            ctxManager = (TransactionContextManager) kernel.getGBean(TransactionContextManager.class);
        } catch (GBeanNotFoundException except) {
            throw new RuntimeException( "Can't lookup GBean: " + TransactionContextManager.class, except);
        }

        MultiParentClassLoader loader = (MultiParentClassLoader) ctxManager.getClass().getClassLoader();

        // Add Jencks to Geronimo's root classloader to avoid InvalidAccessError
        AbstractNameQuery abstractNameQuery = new AbstractNameQuery(null, Collections.EMPTY_MAP, Repository.class.getName());
        Set set = kernel.listGBeans(abstractNameQuery);
        for (Iterator iterator = set.iterator(); iterator.hasNext();) {
            AbstractName abstractName = (AbstractName) iterator.next();
            File f = null;
            try {
                Repository repo = (Repository) kernel.getGBean(abstractName);
                f = repo.getLocation(new Artifact("org.jencks", "jencks", "1.3", "jar"));
                loader.addURL(f.toURL());
            } catch (GBeanNotFoundException except) {
                throw new RuntimeException("Can't lookup GBean: " + abstractName, except);
            } catch (MalformedURLException except) {
                throw new RuntimeException("Invalid URL for jencks: " + f, except);
            }
        }

        // Use Jenck to wrap TransactionContextManager back to TransactionManager
        return new GeronimoTransactionManager(ctxManager);
    }

}
