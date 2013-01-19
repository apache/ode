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
    
    public static CompositeProcess compose(ReceiveProcess<?> process) {
        CompositeProcess result = new CompositeProcess();
        return result.or(process);
    }

    @SuppressWarnings("serial")
	public static <T extends Channel> ChannelListener receive(T proxy, T listener) {
    	// TODO: NOTE: this *only* works when the listnere doesn't need to be Serialiazble really
    	//  because we cannot declare a staic serialVersionUID like this
    	//  once we fix serialization, this can be simplified significantly via a dsl
    	return new ReceiveProcess<T>(proxy, listener) {
    		// private static final long serialVersionUID = 1024137371118887935L;
        };
    }
}
