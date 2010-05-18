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

package org.apache.ode.axis2;

import junit.framework.TestCase;

import javax.servlet.ServletException;
import java.io.File;

import org.junit.Test;

/**
 *
 */
public class ODEServerTest extends TestCase {

    public void testNonExistingRootDir() {
        String ghostDir = "/" + System.currentTimeMillis();
        assertFalse("This test requires a non existing directory", new File(ghostDir).isDirectory());
        System.setProperty("org.apache.ode.rootDir", ghostDir);
        try {
            new ODEServer().init((String) null, null);
            fail("Should throw an IllegalArgumentException if the root dir does not exist");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (ServletException se) {
            fail("Should throw an IllegalArgumentException if the root dir does not exist");
        }finally {
            // reset to avoid side effects
            System.getProperties().remove("org.apache.ode.rootDir");
        }
    }

    public void testNonExistingConfigDir() {
        String ghostDir = "/" + System.currentTimeMillis();
        assertFalse("This test requires a non existing directory", new File(ghostDir).isDirectory());
        System.setProperty("org.apache.ode.configDir", ghostDir);
        try {
            new ODEServer().init(System.getProperty("user.dir"), null);
            fail("Should throw an IllegalArgumentException if the config dir does not exist");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        } catch (ServletException se) {
            fail("Should throw an IllegalArgumentException if the config dir does not exist");
        }finally {
            // reset to avoid side effects
            System.getProperties().remove("org.apache.ode.configDir");
        }
    }
}
