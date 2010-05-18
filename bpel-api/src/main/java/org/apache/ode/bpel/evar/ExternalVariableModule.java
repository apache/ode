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

package org.apache.ode.bpel.evar;

import java.util.Date;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Representation of an external source for variable data.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public interface ExternalVariableModule {

    /**
     * Get the QName of this external variable engine; this method must always return a valid non-null value. 
     * The name of the external variable engine is used to identify it in the external variable declaration.
     * @return
     */
    public QName getName();

    
    /** 
     * Start the external variable subsystem. This method is called before the engine is started. 
     *
     */
    public void start();
    
    /**
     * Stop the external variable subsystem. This method is called right after the engine is stopped.
     *
     */
    public void stop();
    
    /**
     * Shutdown the external variable subsystem. This method is called right after the engine is shutdown. 
     *
     */
    public void shutdown();
    
    
    /**
     * Report whether this engine is transactional, i.e. do the update/fetch methods use the JTA TX?  
     * @return <code>true</code> if transactional, <code>false</code> otherwsie.
     */
    public boolean isTransactional();
    
 
    /**
     * Configure an external variable. 
     * @param pid process 
     * @param extVarId external variable identifier
     * @param config configuration element
     * @throws ExternalVariableModuleException 
     */
    public void configure(QName pid, String extVarId, Element config) throws ExternalVariableModuleException;

    
    /**
     * The the value of an external variable. 
     * @param locator variable locator
     * @param initialize indicates if this is the first time the value is being read
     * @return value of the variable
     */
    public Value readValue(QName varType, Locator locator) throws ExternalVariableModuleException;
    
    /**
     * Update the value of the external variable.
     * @param newval new variable value 
     * @param initialize indicates if this is a variable initialization
     */
    public Value writeValue(QName varType, Value newval) throws ExternalVariableModuleException;
    
    
    /**
     * Structure used to identify an external variable to the external variable subsystem.
     */
    public class Locator {
        
        private static final long serialVersionUID = 1L;

        public final String varId;
        
        /** Instance identifier. */
        public final Long iid;
        
        /** Process identifier. */
        public final QName pid;
        
        /** External-variable module-specific identifying information. */
        public Node reference;
                
        public Locator(String varId, QName pid, Long iid) {
            this(varId, pid, iid, null);
        }

        public Locator(String varId, QName pid, Long iid, Node reference) {
            this.varId = varId;
            this.pid = pid;
            this.iid = iid;
            this.reference = reference;
        }
        
    }

    /**
     * Data structure used to report the value of the variable to the BPEL engine from the external
     * sub system.
     */
    public class Value {
        /** Variable locator. See {@link Locator}. */
        public final Locator locator; 
        
        /** Value of the variable. */
        public final Node value;
        
        /** Advisory indicating when the variable becomes stale (or null if non-perishable). */
        public final Date useByDate;
        
        public Value(Locator locator, Node value) {
            this(locator,value, null);
        }
        
        public Value(Locator locator, Node value, Date useByDate) {
            this.locator = locator;
            this.value = value;
            this.useByDate = useByDate;
        }
    }


    

}
