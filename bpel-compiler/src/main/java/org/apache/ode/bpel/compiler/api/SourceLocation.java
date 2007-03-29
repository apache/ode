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
package org.apache.ode.bpel.compiler.api;

import java.net.URI;

/**
 * Interface for objects that have some relation to a particular
 * location in a source file.
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public interface SourceLocation {
    /**
     * URI of the source file.
     * @return
     */
    URI getURI();
    
    /**
     * Line number.
     * @return
     */
    int getLineNo();
    
    /**
     * Column number.
     * @return
     */
    int getColumnNo();
    
    /**
     * Location path (not a file path, but a path in an expression).
     * @return
     */
    String getPath();
}
