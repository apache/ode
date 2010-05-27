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
package org.apache.ode.ra.transports;

import java.rmi.RemoteException;
import java.util.Properties;


/**
 * Interface implemented communication transports for the ODE JCA adapter.
 */
public interface OdeTransport {

  /**
   * Create a new communication pipe.
   * @param url connection URL
   * @param properties transport-specific communication properties
   * @return new communication pipe
   * @throws RemoteException in case of communication failure
   */
  public OdeTransportPipe createPipe(String url, Properties properties)
          throws RemoteException ;

}
