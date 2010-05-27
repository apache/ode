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
package org.apache.ode.bpel.iapi;

import java.util.Properties;

import org.apache.ode.bpel.evt.BpelEvent;

/**
 * Listener interface implemented by parties interested in receiving
 * {@link org.apache.ode.bpel.evt.BpelEvent}.
 *
 * @author mszefler
 *
 */
public interface BpelEventListener {

    /**
     * Handle events.
     * @param bpelEvent BPEL event
     */
    void onEvent(BpelEvent bpelEvent);

    /**
     * Allows the initialisation of listeners. Called directly
     * after removing the listener from the listeners list.
     *
     * <code>configProperties</code> provide access to
     * configuration option defined in Ode's configuration file
     * (depends on the used IL implementation). This parameter might
     * be null if no configuration options are available (i.e. in test
     * cases).
     *
     * @param configProperties configuration properties
     *
     */
    void startup(Properties configProperties);

    /**
     * Allows the clean up in listener implementations. Called
     * directly before adding the listener to the listeners list.
     */
    void shutdown();

}
