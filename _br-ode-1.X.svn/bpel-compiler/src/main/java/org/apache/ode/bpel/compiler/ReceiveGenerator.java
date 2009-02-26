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
package org.apache.ode.bpel.compiler;

import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.ReceiveActivity;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OEmpty;
import org.apache.ode.bpel.o.OPickReceive;

class ReceiveGenerator extends PickReceiveGenerator {

  public void compile(OActivity output, Activity src) {
    OPickReceive opick = (OPickReceive) output;
    ReceiveActivity rcvDef = (ReceiveActivity) src;

    opick.createInstanceFlag = rcvDef.isCreateInstance();
    OPickReceive.OnMessage onMessage = compileOnMessage(
            rcvDef.getVariable(),
            rcvDef.getPartnerLink(),
            rcvDef.getOperation(),
            rcvDef.getMessageExchangeId(),
            rcvDef.getPortType(),
            rcvDef.isCreateInstance(),
            rcvDef.getCorrelations(),
            rcvDef.getRoute());

    onMessage.activity = new OEmpty(_context.getOProcess(), opick);
    opick.onMessages.add(onMessage);
  }

}
