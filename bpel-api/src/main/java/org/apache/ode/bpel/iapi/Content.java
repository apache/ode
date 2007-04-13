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

/**
 * Opaque representation of content. Implementation provided by integration
 * layer. In the engine, we don't manipulate content directly, so we 
 * don't care what this is. Excpetions are BPEL data manipulation activities 
 * (like ASSIGN) and expressions: implementations of these will have to be
 * specific to content (via a ContentHandler mechanism).  
 * @todo define content handler mechanism
 */
public interface Content {

}
