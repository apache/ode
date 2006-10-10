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

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.transaction.context.GeronimoTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import javax.transaction.TransactionManager;
import javax.management.ObjectName;
import java.net.URI;
import java.net.URISyntaxException;

public class GeronimoFactory {

    public GeronimoFactory() {
    }

    public TransactionManager getTransactionManager() {
        Kernel kernel = KernelRegistry.getSingleKernel(); 
        try {
            // Using Jencks.
            TransactionContextManager ctxManager = (TransactionContextManager)kernel.getProxyManager().createProxy(
                new AbstractName(new URI("geronimo/j2ee-server/1.1/car?ServiceModule=geronimo/j2ee-server/1.1/car,j2eeType=TransactionContextManager,name=TransactionContextManager")),
                TransactionContextManager.class);
            return new GeronimoTransactionManager(ctxManager);
/*
            // Passing TransactionManager directly.
            TransactionManager txManager = (TransactionManager)kernel.getProxyManager().createProxy(
                new AbstractName(new URI("geronimo/j2ee-server/1.1/car?ServiceModule=geronimo/j2ee-server/1.1/car,j2eeType=TransactionManager,name=TransactionManager")),
                TransactionManager.class);
            return txManager;
*/
        } catch (URISyntaxException except) {
            throw new RuntimeException(except);
        }
    }

}
