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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClusterUrlTransformer {
    private static final Log __log = LogFactory.getLog(ClusterUrlTransformer.class);

    private final List<String> targets;
    private final String base;
    
    public ClusterUrlTransformer(List<String> targets, String base) {
        super();
        this.targets = targets;
        this.base = base;
    }

    public String rewriteOutgoingClusterURL(String url1) {
       String url = url1;
       for (String target : targets) {
           if (target.length() > 0) {
               url = url.replace(target, base);
           }
       }
       if (__log.isDebugEnabled()) {
           __log.debug("targets: " + targets + " base: " + base);
           __log.debug("rewrite: " + url1 + " to " + url);
       }
       return url;
    }
}
