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
package org.apache.ode.jacob;

/**
 * Interface implemented by JACOB objects that are to be indexed. Indexed objects have the nice feature that they
 * can be retrieved from the execution queue by their index key. This is handy for introspecting the state of
 * the execution queue. Note that indexed objects are made available so long as they are referenced in some way by
 * objects in the queue. The reference does not need to be direct; e.g. if a {@link org.apache.ode.jacob.ChannelListener}
 * in the execution queue references an indexed object, that indexed object will be indexed.  
 * 
 * @author Maciej Szefler - m s z e f l e r @ g m a i l . c o m
 *
 */
public interface IndexedObject {

    /**
     * Get the value of the object's index.
     * @return
     */
    public Object getKey();
    
}
