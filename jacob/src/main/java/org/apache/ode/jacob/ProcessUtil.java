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
package org.apache.ode.jacob;

import org.apache.ode.jacob.vpu.JacobVPU;


public final class ProcessUtil {
    private ProcessUtil() {
        // Utility class
    }

    public static String exportChannel(Channel channel) {
    	if (channel != null && channel instanceof ChannelProxy) {
            // TODO: replace the brute force call on the activeThread with
            //  something that doesn't expose the implementation once the
            //  cleaner refactored api becomes available
            return JacobVPU.activeJacobThread().exportChannel(channel);
    	}
        throw new IllegalArgumentException("Invalid proxy type: "
    	    + channel == null ? "<null>" : channel.getClass().toString());
    }
}
