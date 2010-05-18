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

package org.apache.ode.bpel.dao;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * This DAO handles any process and instance management related database
 * operations. The idea is to separate out the operational side of database
 * tasks from core engine.
 * 
 * @author sean
 * 
 */
public interface ProcessManagementDAO {
    public static class InstanceSummaryKey {
        public final String pid;
        public final String instanceStatus;

        public InstanceSummaryKey(String pid, String instanceStatus) {
            super();
            this.pid = pid;
            this.instanceStatus = instanceStatus;
        }

        @Override
        public String toString() {
            return "InstanceSummaryKey [instanceStatus=" + instanceStatus
                    + ", pid=" + pid + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime
                    * result
                    + ((instanceStatus == null) ? 0 : instanceStatus.hashCode());
            result = prime * result + ((pid == null) ? 0 : pid.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            InstanceSummaryKey other = (InstanceSummaryKey) obj;
            if (instanceStatus == null) {
                if (other.instanceStatus != null)
                    return false;
            } else if (!instanceStatus.equals(other.instanceStatus))
                return false;
            if (pid == null) {
                if (other.pid != null)
                    return false;
            } else if (!pid.equals(other.pid))
                return false;
            return true;
        }
        
    }
    
    public static class FailedSummaryValue {
        public final Long count;
        public final Date lastFailed;
        public FailedSummaryValue(Long count, Date lastFailed) {
            super();
            this.count = count;
            this.lastFailed = lastFailed;
        }
    }

    public Map<InstanceSummaryKey, Long> countInstancesSummary(Set<String> pids);
    
    public Map<String, FailedSummaryValue> findFailedCountAndLastFailedDateForProcessIds(Set<String> pids);
}