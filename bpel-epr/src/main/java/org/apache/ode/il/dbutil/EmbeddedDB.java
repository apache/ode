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

package org.apache.ode.il.dbutil;

import org.apache.ode.il.config.OdeConfigProperties;

public class EmbeddedDB extends InternalDB {

    public EmbeddedDB(OdeConfigProperties props) {
        super(props);
    }
    
    /**
     * Initialize embedded (DERBY) database.
     */
    @Override
    protected void initDataSource() throws DatabaseConfigException {
        String db = _odeConfig.getDbEmbeddedName();

        String url = "jdbc:derby:" + _workRoot + "/" + db ;
        __log.info("Using Embedded Derby: " + url);
        _derbyUrl = url;
        initInternalDb(url, org.apache.derby.jdbc.EmbeddedDriver.class.getName(),"sa",null);
        _needDerbyShutdown = true;
    }

}
