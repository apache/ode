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
package org.apache.ode.test;

import org.junit.Ignore;
import org.junit.Test;

public class PubSubTest extends BPELTestAbstract {
	@Ignore @Test public void testPubSubInProc() throws Throwable {
        go("/bpel/2.0/TestPubSubInProc");
    }

	@Ignore @Test public void testPubSubOutOfProc() throws Throwable {
        go("/bpel/2.0/TestPubSubOutOfProc");
    }

	@Override
	protected long getWaitBeforeInvokeTimeout() {
		return 10000;
	}
}
