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

package org.apache.ode.bpel.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.dd.TInvoke;
import org.apache.ode.bpel.dd.TProvide;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.o.OPartnerLink;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.apache.ode.utils.msg.MessageBundle;
import org.w3c.dom.Element;

/**
 * Extracts and treats process deployment descriptors. The descriptor interacts
 * with the process deployment either by modifying the {@link org.apache.ode.bpel.dao.ProcessDAO}.
 */
class ProcessDDInitializer {

    private static final Messages __msgs = MessageBundle
            .getMessages(Messages.class);

    private static final Log __log = LogFactory.getLog(ProcessDDInitializer.class);

    private OProcess _oprocess;
    private TDeployment.Process _dd;

    public ProcessDDInitializer(OProcess oprocess, TDeployment.Process dd) {
        _oprocess = oprocess;
        _dd = dd;
    }

    public void update(ProcessDAO processDAO) {
        handleEndpoints(processDAO);
        handleProperties(processDAO);
    }

    private void handleEndpoints(ProcessDAO processDAO) {
        for (TProvide provide : _dd.getProvideList()) {
            OPartnerLink pLink = _oprocess.getPartnerLink(provide.getPartnerLink());
            if (pLink == null) {
                String msg = ProcessDDInitializer.__msgs.msgDDPartnerLinkNotFound(provide.getPartnerLink());
                ProcessDDInitializer.__log.error(msg);
                throw new BpelEngineException(msg);
            }
            if (!pLink.hasMyRole()) {
                String msg = ProcessDDInitializer.__msgs.msgDDMyRoleNotFound(provide.getPartnerLink());
                ProcessDDInitializer.__log.error(msg);
                throw new BpelEngineException(msg);
            }
        }
        for (TInvoke invoke : _dd.getInvokeList()) {
            OPartnerLink pLink = _oprocess.getPartnerLink(invoke.getPartnerLink());
            if (pLink == null) {
                String msg = ProcessDDInitializer.__msgs.msgDDPartnerLinkNotFound(invoke.getPartnerLink());
                ProcessDDInitializer.__log.error(msg);
                throw new BpelEngineException(msg);
            }
            if (!pLink.hasPartnerRole()) {
                String msg = ProcessDDInitializer.__msgs.msgDDPartnerRoleNotFound(invoke.getPartnerLink());
                ProcessDDInitializer.__log.error(msg);
                throw new BpelEngineException(msg);
            }
            // TODO Handle non initialize partner roles that just provide a binding
            if (!pLink.initializePartnerRole && _oprocess.version.equals(Namespaces.WS_BPEL_20_NS)) {
                String msg = ProcessDDInitializer.__msgs.msgDDNoInitiliazePartnerRole(invoke.getPartnerLink());
                ProcessDDInitializer.__log.error(msg);
                throw new BpelEngineException(msg);
            }
        }
    }

    private void handleProperties(ProcessDAO processDAO) {
        if (_dd.getPropertyList().size() > 0) {
            for (TDeployment.Process.Property property : _dd.getPropertyList()) {
                String textContent = DOMUtils.getTextContent(property.getDomNode());
                if (textContent != null) {
                    processDAO.setProperty(property.getName().getLocalPart(), property.getName().getNamespaceURI(),
                            textContent);
                } else {
                    Element elmtContent = DOMUtils.getElementContent(property
                            .getDomNode());
                    processDAO.setProperty(property.getName().getLocalPart(), property.getName().getNamespaceURI(),
                            elmtContent);
                }
            }
        }
    }


    public boolean exists() {
        return _dd != null;
    }

    public void init(ProcessDAO newDao) {
        newDao.setRetired(_dd.getRetired());
        newDao.setActive(_dd.getActive() || !_dd.isSetActive());
        for (String correlator : _oprocess.getCorrelators()) {
            newDao.addCorrelator(correlator);
        }
    }

}