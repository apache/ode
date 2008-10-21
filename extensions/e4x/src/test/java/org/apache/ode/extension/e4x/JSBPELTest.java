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
package org.apache.ode.extension.e4x;

import org.apache.log4j.BasicConfigurator;
import org.apache.ode.test.BPELTestAbstract;
import org.junit.Test;

/**
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class JSBPELTest extends BPELTestAbstract {

	@Test public void testE4XAssign() throws Throwable {
        // Test E4X
		registerExtensionBundle(new JSExtensionBundle());
        go("/bpel/TestE4X");
    }

	@Test public void testE4XAssignDirect() throws Throwable {
        // Test E4X
		BasicConfigurator.configure();
		registerExtensionBundle(new JSExtensionBundle());
        go("/bpel/TestE4XDirect");
    }

}
