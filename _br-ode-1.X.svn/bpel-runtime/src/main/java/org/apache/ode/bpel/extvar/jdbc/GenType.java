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
 * Generator type enumaration.
 * 
 * @author Maciej Szefler <mszefler at gmail dot com>
 *
 */
enum GenType {
    /** plain old column */
    none,
    
    /** sequence column */
    sequence,
    
    /** SQL expression column */
    expression,
    
    /** server-generated uuid column */
    uuid, 
    
    /** process-id column */
    pid, 
    
    /** instance-id column */
    iid, 
    
    /** create timestamp */
    ctimestamp, 
    
    /** update timestamp */
    utimestamp
}