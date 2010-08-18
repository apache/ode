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

import org.apache.ode.bpel.bdi.breaks.ActivityBreakpoint;
import org.apache.ode.bpel.bdi.breaks.Breakpoint;
import org.apache.ode.bpel.bdi.breaks.VariableModificationBreakpoint;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.dao.*;
import org.apache.ode.bpel.iapi.BpelServer;
import org.apache.ode.bpel.iapi.ProcessStore;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.pmapi.*;
import org.apache.ode.bpel.pmapi.TInstanceInfo.EventInfo;
import org.apache.ode.bpel.runtime.breaks.ActivityBreakpointImpl;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.uuid.UUIDGen;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Implementation of the instance/process management interaction. This class implements
 * the methods necessary to support process debugging. It also implements all the methods in the
 * newer Process/Instance Management interface (pmapi).
 */
public class BpelManagementFacadeImpl extends ProcessAndInstanceManagementImpl
        implements BpelManagementFacade {
    private static UUIDGen _uuidGen = new UUIDGen();

    public BpelManagementFacadeImpl(BpelServer server, ProcessStore store) {
        super(server, store);
    }

    public short getState(final Long iid) throws ManagementException {

        return dbexec(new BpelDatabase.Callable<Short>()  {
            public Short run(BpelDAOConnection session) throws Exception {
                ProcessInstanceDAO instance = session.getInstance(iid);
                return Short.valueOf(instance.getState());
            }
        }).shortValue();

    }


    public Long getProcessInstance(String pid, final CorrelationKey ckey) throws ManagementException {

        return dbexec(_db.getProcessDb(QName.valueOf(pid)).new Callable<Long>()  {
            public Long run(BpelDAOConnection session) throws Exception {
                Iterator<ProcessInstanceDAO> i = getProcessDAO().findInstance(ckey).iterator();
                return (i.hasNext()) ? i.next().getInstanceId() : null;
            }
        });
    }

    public EventInfoListDocument getEvents(final Long iid, final int startIdx, final int count)
            throws ManagementException {

        // TODO: this is a bit of hack, if there are two events with exactly the
        // same timestamp, we can have more events returned then requested.

        List<String> timeline;

        try {
            timeline = getEventTimeline("iid="+iid,null);
        } catch (Exception ex) {
            throw new ManagementException("Unable to retrieve timeline.", ex);
        }

        if (startIdx >= timeline.size())
            return EventInfoListDocument.Factory.newInstance();

        timeline = timeline.subList(startIdx,Math.min(timeline.size(),startIdx+count));
        String startdt = timeline.get(0);
        String enddt = timeline.get(timeline.size()-1);

        return listEvents("iid="+iid,"timestamp>="+startdt +" " +"timestamp<="+enddt,0);
    }

    public int getEventCount(final Long iid) throws ManagementException {
        EventInfo einfo = getInstanceInfo(iid).getInstanceInfo().getEventInfo();
        if (einfo == null)
            return 0;
        return einfo.getCount();
    }



    public String getVariable(final Long iid, final Long scopeId, final String varName)
            throws ManagementException {
        return dbexec(new BpelDatabase.Callable<String>()  {
            public String run(BpelDAOConnection session) throws Exception {
                ProcessInstanceDAO instance = session.getInstance(iid);
                if (instance == null) {
                    throw new ManagementException("InstanceNotFound:" + iid);
                }
                ScopeDAO scope = instance.getScope(scopeId);
                if (scope == null) {
                    throw new ManagementException("ScopeNotFound:" + scopeId);
                }
                XmlDataDAO var = scope.getVariable(varName);
                if (var == null) {
                    throw new ManagementException("VarNotFound:" + varName);
                }
                return nodeToString(var);
            }
        });
    }

    public Long[] getScopeInstancesForScope(final Long iid, final String scopeName) throws ManagementException {

        return dbexec(new BpelDatabase.Callable<Long[]>()  {
            public Long[] run(BpelDAOConnection session) throws Exception {
                ProcessInstanceDAO instance = session.getInstance(iid);
                if (instance == null) {
                    throw new ManagementException("InstanceNotFound:" + iid);
                }
                Collection<ScopeDAO> scopes = instance.getScopes(scopeName);
                List<Long>instanceIds = new ArrayList<Long>(scopes.size());
                for(ScopeDAO i : scopes) {
                    instanceIds.add(i.getScopeInstanceId());
                }
                return instanceIds.toArray(new Long[scopes.size()]);
            }
        });
    }

    public void setVariable(Long pid, Long scopeId, String varName, String data) {
        throw new UnsupportedOperationException();
    }

    public void setCorrelation(final Long iid, final Long scopeId, final String correlationSet,
                               final QName[] propertyNames, final CorrelationKey values)
            throws ManagementException {

        dbexec(new BpelDatabase.Callable<Object>()  {
            public Object run(BpelDAOConnection session) throws Exception {
                ProcessInstanceDAO instance = session.getInstance(iid);
                if (instance == null) {
                    throw new ManagementException("InstanceNotFound:" + iid);
                }
                CorrelationSetDAO correlationSetDAO = instance.getCorrelationSet(correlationSet);
                correlationSetDAO.setValue(propertyNames, values);
                return null;
            }
        });

    }

    public CorrelationKey getCorrelation(final Long iid, final Long scopeId, final String correlationSet) throws ManagementException {
        return dbexec(new BpelDatabase.Callable<CorrelationKey>()  {
            public CorrelationKey run(BpelDAOConnection session) throws Exception {
                ProcessInstanceDAO instance = session.getInstance(iid);
                if (instance == null) {
                    throw new ManagementException("InstanceNotFound:" + iid);
                }
                CorrelationSetDAO corr = findCorrelationSetDAO(instance, scopeId, correlationSet);
                return corr.getValue();
            }
        });
    }

    /**
     * @see BpelManagementFacade#getProcessDef(String)
     * @param procid
     */
    public OProcess getProcessDef(String procid) throws ManagementException {
        if (_server._engine == null)
            throw new ProcessingException("ServiceProvider required for debugger operation.");

        BpelProcess process = _server._engine._activeProcesses.get(QName.valueOf(procid));
        if (process == null)
            throw new InvalidRequestException("The process \"" + procid + "\" is not available. Please make sure it is deployed and encompassing System is activated." );

        return process.getOProcess();
    }

    public void step(final Long iid) throws ManagementException {
        // We need debugger support in order to resume (since we have to force
        // a reduction. If one is not available the getDebugger() method should
        // throw a ProcessingException
        DebuggerSupport debugSupport = getDebugger(iid);
        assert debugSupport != null : "getDebugger(Long) returned NULL!";
        debugSupport.step(iid);
    }

    public QName getCompletedFault(final Long iid) throws ManagementException {

        return dbexec(new BpelDatabase.Callable<QName>()  {
            public QName run(BpelDAOConnection session) throws Exception {
                ProcessInstanceDAO instance = session.getInstance(iid);
                if (instance == null)
                    throw new ManagementException("InstanceNotFound:" + iid);
                return instance.getFault().getName();
            }
        });
    }

    public Breakpoint[] getGlobalBreakpoints(String procId) throws ManagementException {
        DebuggerSupport debuggerSupport = getDebugger(QName.valueOf(procId));
        return debuggerSupport.getGlobalBreakpoints();
    }

    public Breakpoint[] getBreakpoints(Long iid) throws ManagementException {
        DebuggerSupport debuggerSupport = getDebugger(iid);
        return debuggerSupport.getBreakpoints(iid);
    }


    public void removeGlobalBreakpoint(String procid, Breakpoint sp) throws ManagementException {
        DebuggerSupport debuggerSupport = getDebugger(QName.valueOf(procid));
        debuggerSupport.removeGlobalBreakpoint(sp);
    }

    public void removeBreakpoint(Long iid, Breakpoint sp) throws ManagementException {
        DebuggerSupport debuggerSupport = getDebugger(iid);
        debuggerSupport.removeBreakpoint(iid, sp);
    }

    public ActivityBreakpoint addGlobalActivityBreakpoint(String procid, String activity) throws ManagementException {
        DebuggerSupport debuggerSupport = getDebugger(QName.valueOf(procid));

        ActivityBreakpointImpl bp = new ActivityBreakpointImpl(_uuidGen.nextUUID(), activity);
        debuggerSupport.addGlobalBreakpoint(bp);
        return bp;
    }

    public ActivityBreakpoint addActivityBreakpoint(Long iid, String activity) throws ManagementException {
        DebuggerSupport debuggerSupport = getDebugger(iid);

        ActivityBreakpointImpl bp = new ActivityBreakpointImpl(_uuidGen.nextUUID(), activity);
        debuggerSupport.addBreakpoint(iid, bp);
        return bp;
    }

    public VariableModificationBreakpoint addVariableModificationBreakpoint(Long iid, String scopename, String variable) {
        throw new UnsupportedOperationException();
    }

    public Date getStartTime(final Long iid) throws ManagementException {

        return dbexec(new BpelDatabase.Callable<Date>()  {
            public Date run(BpelDAOConnection session) throws Exception {
                ProcessInstanceDAO instance = session.getInstance(iid);
                if (instance == null)
                    throw new InstanceNotFoundException("InstanceNotFound:" + iid);
                return instance.getCreateTime();
            }
        });
    }

    private CorrelationSetDAO findCorrelationSetDAO(ProcessInstanceDAO instance, Long scopeId, String correlationSet)
            throws ManagementException {
        ScopeDAO scope = instance.getScope(scopeId);
        CorrelationSetDAO corr = null;
        do {
            corr = scope.getCorrelationSet(correlationSet);
            if(corr != null)
                break;
            scope = scope.getParentScope();
        } while(scope != null);

        if(corr == null)
            throw new ManagementException("CorrelationSetNotFound:" + correlationSet);

        return corr;
    }


    private static String nodeToString(XmlDataDAO xml){
        Node data = xml.get();
        String str;
        if (data == null) {
            str = null;
        } else if (data.getNodeType() == Node.ELEMENT_NODE) {
            str = DOMUtils.domToString(data);
        } else {
            str = data.getNodeValue();
        }
        return str;
    }
}
