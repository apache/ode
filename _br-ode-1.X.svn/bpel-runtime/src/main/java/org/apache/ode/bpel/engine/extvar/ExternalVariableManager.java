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
package org.apache.ode.bpel.engine.extvar;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.engine.BpelProcess;
import org.apache.ode.bpel.iapi.BpelEngineException;
import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OElementVarType;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;
import org.apache.ode.bpel.o.OScope.Variable;
import org.apache.ode.bpel.evar.ExternalVariableModule;
import org.apache.ode.bpel.evar.ExternalVariableModuleException;
import org.apache.ode.bpel.evar.ExternalVariableModule.Locator;
import org.apache.ode.bpel.evar.ExternalVariableModule.Value;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Manager for external variable instances; used by {@link BpelProcess} to manage external variables.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public class ExternalVariableManager {

    private static final Log __log = LogFactory.getLog(ExternalVariableManager.class);

    /** Mapping of engine names to engine. */
    private Map<QName, ExternalVariableModule> _engines;

    /** Mapping between external variable identifiers and the engines that are used to serve them up. */
    private final Map<String, EVar> _externalVariables = new HashMap<String, EVar>();

    /** The configuration. */
    private final ExternalVariableConf _extVarConf;

    /** Process ID */
    private QName _pid;

    public ExternalVariableManager(QName pid,
            ExternalVariableConf evconf, 
            Map<QName, ExternalVariableModule> engines, 
            OProcess oprocess)
            throws BpelEngineException {
        _pid = pid;
        _extVarConf = evconf;
        _engines = engines;

        boolean fatal = false;

        // Walk the configuration and find all the variabless.
        for (ExternalVariableConf.Variable var : _extVarConf.getVariables()) {
            EVar evar = new EVar(var.extVariableId, _engines.get(var.engineQName), var.configuration);
            if (evar._engine == null) {
                __log.error("External variable engine \"" + var.engineQName 
                        + "\" referenced by external variable \"" + var.extVariableId
                        + "\" not registered.");
                fatal = true;
                continue;
            }
            
            try {
                evar._engine.configure(_pid, evar._extVarId, evar._config);
            } catch (ExternalVariableModuleException eve) {
                __log.error("External variable subsystem configuration error.", eve);
                throw new BpelEngineException("External variable subsystem configuration error.",eve);
            }
            if (_externalVariables.containsKey(var.extVariableId)) {
                __log.warn("Duplicate external variable configuration for \"" + var.extVariableId + "\" will be ignored!");
            }
            _externalVariables.put(var.extVariableId, evar);
        }

        // Walk down the process definition looking for any external variables.
        for (OBase child : oprocess.getChildren()) {
            if (!(child instanceof OScope))
                continue;
            OScope oscope = (OScope) child;
            for (OScope.Variable var : oscope.variables.values()) {
                if (var.extVar == null)
                    continue;

                EVar evar = _externalVariables.get(var.extVar.externalVariableId);
                if (evar == null) {
                    __log.error("The \"" + oscope.name + "\" scope declared an unknown external variable \""
                            + var.extVar.externalVariableId + "\"; check the deployment descriptor.");
                    fatal = true;
                    continue;
                }

            }
        }

        if (fatal) {
            String errmsg = "Error initializing external variables. See log for details.";
            __log.error(errmsg);
            throw new BpelEngineException(errmsg);
        }

    }


    /**
     * Read an external variable.
     */
    public Value read(Variable variable, Node reference, Long iid) throws ExternalVariableModuleException{
        EVar evar = _externalVariables.get(variable.extVar.externalVariableId);
        if (evar == null) {
            // Should not happen if constructor is working.
            throw new BpelEngineException("InternalError: reference to unknown external variable " + variable.extVar.externalVariableId);
        }
        
        Locator locator = new Locator(variable.extVar.externalVariableId, _pid,iid, reference);
        Value newval;
        newval = evar._engine.readValue(((OElementVarType) variable.type).elementType, locator );
        if (newval == null)
            return null;
        return newval;
    }

    
    public Value write(Variable variable, Node reference, Node val, Long iid) throws ExternalVariableModuleException  {
        EVar evar = _externalVariables.get(variable.extVar.externalVariableId);
        if (evar == null) {
            // Should not happen if constructor is working.
            throw new BpelEngineException("InternalError: reference to unknown external variable " + variable.extVar.externalVariableId);
        }
        
        Locator locator = new Locator(variable.extVar.externalVariableId,_pid,iid,reference);
        Value newval = new Value(locator,val,null);
        newval = evar._engine.writeValue(((OElementVarType) variable.type).elementType, newval);

        return newval;
    }

   
    static final class EVar {
        final ExternalVariableModule _engine;

        final Element _config;

        final String _extVarId;

        EVar(String id, ExternalVariableModule engine, Element config) {
            _extVarId = id;
            _engine = engine;
            _config = config;
        }
    }


}
