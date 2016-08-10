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

package org.apache.ode.tomee;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.tomee.embedded.Configuration;
import org.apache.tomee.embedded.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TomeeLauncher {
    private static final Logger log = LoggerFactory.getLogger(TomeeLauncher.class);

    private Properties properties = new Properties();
    private File odeTomeeHomeDir = null;
    private File odeTomeeConfDir = null;

    Container container = null;

    public TomeeLauncher(String homeDir) {
        this.odeTomeeHomeDir = new File(homeDir);
    }

    public void initialize() throws Exception {
        log.info("Class {} loaded by {}",TomeeLauncher.class, TomeeLauncher.class.getClassLoader());

        if(!odeTomeeHomeDir.exists()) {
            throw new Exception("ODE home directory doesn't exist");
        }

        this.odeTomeeConfDir = new File(odeTomeeHomeDir,"conf");

        if(!odeTomeeConfDir.exists()) {
            throw new Exception("Tomee conf directory doesn't exist");
        }

        properties = loadConfigFile(new File(odeTomeeConfDir,Constants.ODE_TOMEE_CONFIG));

        Configuration configuration = setupConfiguration(properties);
        setupContiner(configuration);
    }

    protected Configuration setupConfiguration(Properties properties) {
        Configuration configuration = new Configuration();

        configuration.setDir(properties.getProperty(Constants.DIR, new File(odeTomeeHomeDir,"work").getAbsolutePath()));
        configuration.setHost(properties.getProperty(Constants.HOST,"localhost"));
        configuration.setHttp2(Boolean.valueOf(properties.getProperty(Constants.HTTP2,"false")));
        configuration.setHttpPort(Integer.valueOf(properties.getProperty(Constants.HTTP_PORT,"8080")));
        configuration.setHttpsPort(Integer.valueOf(properties.getProperty(Constants.HTTPS_PORT,"8443")));
        configuration.setKeepServerXmlAsThis(Boolean.valueOf(properties.getProperty(Constants.KEEP_SERVER_XML_AS_THIS,"true")));
        configuration.setKeyAlias(properties.getProperty(Constants.KEY_ALIAS));
        configuration.setKeystoreFile(properties.getProperty(Constants.KEY_STORE_FILE));
        configuration.setKeystorePass(properties.getProperty(Constants.KEY_STAORE_PASS));
        configuration.setKeystoreType(properties.getProperty(Constants.KEY_STORE_TYPE,"JKS"));

        Properties tomeeSystemProperties = loadConfigFile(new File(odeTomeeConfDir,Constants.TOMEE_SYSTEM_CONFIG));

        tomeeSystemProperties.setProperty("openejb.conf.file", new File(odeTomeeConfDir,Constants.TOMEE_XML).getAbsolutePath());

        configuration.setProperties(tomeeSystemProperties);
        configuration.setQuickSession(Boolean.valueOf(properties.getProperty(Constants.QUICK_SESSION,"false")));
        configuration.setServerXml(properties.getProperty(Constants.SERVER_XML,new File(odeTomeeConfDir,Constants.SERVER_XML).getAbsolutePath()));
        configuration.setSkipHttp(Boolean.valueOf(properties.getProperty(Constants.SKIP_HTTP,"false")));
        configuration.setSsl(Boolean.valueOf(properties.getProperty(Constants.SSL,"false")));
        configuration.setSslProtocol(properties.getProperty(Constants.SSL_PROPTOCOL));
        configuration.setStopPort(Integer.valueOf(properties.getProperty(Constants.STOP_PORT,"8005")));
        configuration.setWebResourceCached(Boolean.valueOf(properties.getProperty(Constants.WEB_RESOURCE_CACHED,"false")));
        configuration.setWebXml(properties.getProperty(Constants.WEB_XML,new File(odeTomeeConfDir,Constants.WEB_XML).getAbsolutePath()));

        return configuration;
    }

    protected Container setupContiner(Configuration configuration) {
        this.container = new Container();
        container.setup(configuration);
        return container;
    }

    public void start() throws Exception {
        if(container != null) {
            container.start();
        }
    }

    public void stop() throws Exception {
        if(container != null)
            container.stop();
    }

    public void deploy(File application, String context) throws Exception {
        container.deploy(context, application, true);
        container.await();
    }

    private Properties loadConfigFile(File configFile) {
        FileInputStream io = null;
        Properties properties = new Properties();

        if(configFile != null && configFile.exists()) {
            try {
                io = new FileInputStream(configFile);
                properties.load(io);
            } catch (FileNotFoundException e) {
                log.warn("Configuration file not found {}", configFile);
            } catch (IOException e) {
                log.warn("Error reading configuration file {}", configFile);
            } finally {
                if(io != null) {
                    try {
                        io.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return properties;
    }
}
