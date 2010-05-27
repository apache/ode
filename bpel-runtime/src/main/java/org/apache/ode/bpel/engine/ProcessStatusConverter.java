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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.ScopeStateEnum;
import org.apache.ode.bpel.pmapi.ProcessingException;
import org.apache.ode.bpel.pmapi.TInstanceStatus;
import org.apache.ode.bpel.pmapi.TScopeStatus;

/**
 * Class for converting status codes from external (PMAPI) to internal (DAO) representation.
 */
class ProcessStatusConverter {

  /**
   * A mapping from the interface status codes, to the internal ODE status codes.
   */
  private final Map<TInstanceStatus.Enum, BitSet> __interfaceStatusCodeToInternalStatusCodeMap =
    new HashMap<TInstanceStatus.Enum,BitSet>();

  /**
   * One-to-one map between internal/external representation of the scope
   * status codes.
   */
  private final Map<ScopeStateEnum, TScopeStatus.Enum> __scopeStateMap =
    new HashMap<ScopeStateEnum, TScopeStatus.Enum>();

  ProcessStatusConverter() {

    for (int i = 0 ; i < ProcessState.ALL_STATES.length; ++i){
        short pistate = ProcessState.ALL_STATES[i];
        TInstanceStatus.Enum intstc = cvtInstanceStatus(pistate);
        BitSet bset = __interfaceStatusCodeToInternalStatusCodeMap.get(intstc);
        if (bset == null) {
            bset = new BitSet();
            __interfaceStatusCodeToInternalStatusCodeMap.put(intstc,bset);
        }
        bset.set(pistate);
    }

    __scopeStateMap.put(ScopeStateEnum.ACTIVE, TScopeStatus.ACTIVE);
    __scopeStateMap.put(ScopeStateEnum.COMPLETED,TScopeStatus.COMPLETED);
    __scopeStateMap.put(ScopeStateEnum.FAULT,TScopeStatus.FAULTED);

  }


  /**
   * Convert instance status from the internal database representation to
   * the process management API enumerations.
   * @param instancestate internal database state code
   * @return API-compliant enumeration
   */
  TInstanceStatus.Enum cvtInstanceStatus(short instancestate) {
    switch (instancestate) {
      case ProcessState.STATE_NEW:
      case ProcessState.STATE_READY:
      case ProcessState.STATE_ACTIVE:
        return TInstanceStatus.ACTIVE;
      case ProcessState.STATE_COMPLETED_OK:
        return TInstanceStatus.COMPLETED;
      case ProcessState.STATE_COMPLETED_WITH_FAULT:
        return TInstanceStatus.FAILED;
      case ProcessState.STATE_SUSPENDED:
        return TInstanceStatus.SUSPENDED;
      case ProcessState.STATE_TERMINATED:
        return TInstanceStatus.TERMINATED;
    }

    // TODO: How do we determine if the process is in an "ERROR" state.

    // The above should have been exhaustive.
    throw new ProcessingException("Encountered unexpected instance state: " + instancestate);
  }



  /**
   * The inverse of the above function. The above is not one-to-one, so this
   * functions returns a set.
   * @param status instance status (interface representation)
   * @return internal states corresponding to the requested interface representation.
   */
  short[] cvtInstanceStatus(TInstanceStatus.Enum status) {
    BitSet bset = __interfaceStatusCodeToInternalStatusCodeMap.get(status);
    if (bset == null)
        return new short[0];

    short ret[] = new short[bset.cardinality()];
    for (int i = 0; i < ret.length; ++i) {
        ret[i] = (short)bset.nextSetBit(i==0?0:ret[i-1]+1);
    }

    return ret;
  }


  /**
   * Convert the internal scope state to the external representation.
   * @param status
   * @return
   */
  TScopeStatus.Enum cvtScopeStatus(ScopeStateEnum status) {
     return __scopeStateMap.get(status);
  }
}
