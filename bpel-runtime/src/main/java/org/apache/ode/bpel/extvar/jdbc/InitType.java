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
package org.apache.ode.bpel.extvar.jdbc;

/**
 * Enumeration of methods in which a new external variable row initialization is handled. 
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
public enum InitType {
    /** Just try to update the row, if does not already  exist, fails. */
    update,
    
    /** Just insert the row, if already exist fails. */
    insert,
    
    /** Try updating the row, if no exist, then try inserting. */
    update_insert,
    
    /** First delete the row, then insert a new one. */
    delete_insert

}
