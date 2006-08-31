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

package org.apache.ode.jbi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jbi.management.DeploymentException;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.deploy.DeploymentUnitImpl;

/**
 * Representation of a JBI service unit. A JBI service unit may actually consist
 * of multiple processes.
 */
class OdeServiceUnit {
    private static final Log __log = LogFactory.getLog(OdeServiceUnit.class);

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    /** The ever-present context. */
    private OdeContext _ode;

    /** Our own directory managed by JBI */
    private File _serviceUnitRootPath;

    /** Our JBI indentifier. */
    private String _serviceUnitID;

    /** Ctor. */
    OdeServiceUnit(OdeContext ode, String serviceUnitID, String serviceUnitRootPath) {
        _ode = ode;
        _serviceUnitID = serviceUnitID;
        _serviceUnitRootPath = new File(serviceUnitRootPath);
    }

    public void deploy() throws DeploymentException {

        try {
            _ode._server.deploy(_serviceUnitRootPath);
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeProcessDeploymentFailed(_serviceUnitRootPath, _serviceUnitID);
            __log.error(errmsg, ex);
            throw new DeploymentException(errmsg, ex);
        }
    }

    public void undeploy() throws Exception {
        try {
            _ode._server.undeploy(_serviceUnitRootPath);
        } catch (Exception ex) {
            String errmsg = __msgs.msgOdeProcessUndeploymentFailed(null);
            __log.error(errmsg, ex);
            throw new DeploymentException(errmsg, ex);
        }
    }

    public void init() throws Exception {
        // TODO Auto-generated method stub

    }

    public void shutdown() throws Exception {
        // TODO Auto-generated method stub

    }

    public void start() throws Exception {
//        List<QName> activated = new ArrayList<QName>(_pids.size());
//        Exception e = null;
//        for (QName pid : _pids) {
//            try {
//                _ode._server.activate(pid, false);
//                activated.add(pid);
//            } catch (Exception ex) {
//                e = ex;
//                __log.error("Unable to activate " + pid, ex);
//                break;
//            }
//        }
//        if (activated.size() != _pids.size()) {
//            for (QName pid : activated)
//                try {
//                    _ode._server.deactivate(pid, true);
//                } catch (Exception ex) {
//                    __log.error("Unable to deactivate " + pid, ex);
//                }
//        }
//
//        if (e != null)
//            throw e;
    }

    public void stop() throws Exception {
//        for (QName pid : _pids) {
//            try {
//                _ode._server.deactivate(pid, true);
//            } catch (Exception ex) {
//                __log.error("Unable to deactivate " + pid, ex);
//            }
//        }
    }

}
