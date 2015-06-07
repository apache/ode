/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.ode.clustering.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.*;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * This is to create hazelcast instance.
 * It sets the config object using hazelcast.xml file.First, it looks for the hazelcast.config system property. If it is set, its value is used as the path.
 * Else it will load the hazelcast.xml file using FileSystemXmlConfig()
 */
public class HazelcastInstanceConfig {
    private HazelcastInstance hazelcastInstance;

    public HazelcastInstanceConfig() {
        hazelcastInstance = Hazelcast.newHazelcastInstance();
    }

    /**
     *
     * @param hzXml
     */
    public HazelcastInstanceConfig(File hzXml) {
        try {
            Config config = new FileSystemXmlConfig(hzXml);
            hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        } catch (FileNotFoundException fnf) {
        }
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }
}

