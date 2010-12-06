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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.iapi.DebuggerContext;
import org.apache.ode.bpel.iapi.Scheduler.JobDetails;
import org.apache.ode.bpel.iapi.Scheduler.JobType;
import org.apache.ode.bpel.bdi.breaks.Breakpoint;
import org.apache.ode.bpel.common.ProcessState;
import org.apache.ode.bpel.dao.BpelDAOConnection;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.bpel.evt.ActivityExecStartEvent;
import org.apache.ode.bpel.evt.BpelEvent;
import org.apache.ode.bpel.evt.ProcessCompletionEvent;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.evt.ProcessInstanceStateChangeEvent;
import org.apache.ode.bpel.evt.ProcessTerminationEvent;
import org.apache.ode.bpel.evt.ScopeCompletionEvent;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.pmapi.BpelManagementFacade;
import org.apache.ode.bpel.pmapi.InstanceNotFoundException;
import org.apache.ode.bpel.pmapi.ManagementException;
import org.apache.ode.bpel.pmapi.ProcessingException;
import org.apache.ode.bpel.runtime.breaks.BreakpointImpl;
import org.apache.ode.utils.CollectionUtils;
import org.apache.ode.utils.msg.MessageBundle;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class providing functions used to support debugging funtionality
 * in the BPEL engine. This class serves as the underlying
 * implementation of the {@link BpelManagementFacade} interface, and
 * the various MBean interfaces.
 *
 * @todo Need to revisit the whole stepping/suspend/resume mechanism.
 */
public class DebuggerSupport implements DebuggerContext {

    private static final Log __log = LogFactory.getLog(DebuggerSupport.class);
    private static final Messages __msgs = MessageBundle.getMessages(Messages.class);

    static final Breakpoint[] EMPTY_BP = new Breakpoint[0];

    private boolean _enabled = true;
    private Breakpoint[] _globalBreakPoints = EMPTY_BP;
    private final Set<Long> _step = new HashSet<Long>();
    private final Map<Long, Breakpoint[]>_instanceBreakPoints = new HashMap<Long, Breakpoint[]>();

    /** BPEL process database */
    private BpelProcessDatabase _db;

    /** BPEL process. */
    private BpelProcess _process;

    /**
     * Constructor.
     * @param db BPEL process database
     */
    protected DebuggerSupport(BpelProcess process) {
        _process = process;
        _db = new BpelProcessDatabase(_process._engine._contexts.dao,
                _process._engine._contexts.scheduler,
                _process._pid);

    }

    public void enable(boolean enabled){
        _enabled = enabled;
    }

    public Breakpoint[] getGlobalBreakpoints(){
        return _globalBreakPoints;
    }

    public Breakpoint[] getBreakpoints(Long pid){
        Breakpoint[] arr = _instanceBreakPoints.get(pid);
        return (arr == null)
                ? EMPTY_BP
                : arr;
    }

    public void addGlobalBreakpoint(Breakpoint breakpoint){
        Collection<Breakpoint> c = CollectionUtils.makeCollection(ArrayList.class, _globalBreakPoints);
        c.add(breakpoint);
        _globalBreakPoints = c.toArray(new Breakpoint[c.size()]);
    }

    public void addBreakpoint(Long pid, Breakpoint breakpoint){
        Breakpoint[] bpArr = _instanceBreakPoints.get(pid);
        if(bpArr == null) {
            bpArr = new Breakpoint[]{breakpoint};
        }
        else{
            Collection<Breakpoint> c = CollectionUtils.makeCollection(ArrayList.class, bpArr);
            c.add(breakpoint);
            bpArr = c.toArray(new Breakpoint[c.size()]);
        }
        _instanceBreakPoints.put(pid, bpArr);
    }

    public void removeGlobalBreakpoint(Breakpoint breakpoint){
        Collection<Breakpoint> c = CollectionUtils.makeCollection(ArrayList.class, _globalBreakPoints);
        c.remove(breakpoint);
        _globalBreakPoints = c.toArray(new Breakpoint[c.size()]);
    }

    public void removeBreakpoint(Long pid, Breakpoint breakpoint){
        Breakpoint[] bpArr = _instanceBreakPoints.get(pid);
        if(bpArr != null){
            Collection<Breakpoint> c = CollectionUtils.makeCollection(ArrayList.class, bpArr);
            c.remove(breakpoint);
            bpArr = c.toArray(new Breakpoint[c.size()]);
            if(bpArr.length == 0) {
                _instanceBreakPoints.remove(pid);
            }
            else {
                _instanceBreakPoints.put(pid, bpArr);
            }
        }
    }

    public boolean step(final Long iid) {
        boolean doit = false;

        try {
            doit = _db.exec(new BpelDatabase.Callable<Boolean>() {
                public Boolean run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    if (instance == null)
                        throw new InstanceNotFoundException("" + iid);

                    if(ProcessState.STATE_SUSPENDED == instance.getState()){
                        // send event
                        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
                        evt.setOldState(ProcessState.STATE_SUSPENDED);
                        short previousState = instance.getPreviousState();

                        instance.setState(previousState);

                        evt.setNewState(previousState);
                        evt.setProcessInstanceId(iid);
                        evt.setProcessName(instance.getProcess().getType());
                        evt.setProcessId(_db.getProcessId());

                        _process.saveEvent(evt, instance);

                        onEvent(evt);

                        if (__log.isDebugEnabled()) {
                            __log.debug("step(" + iid + ") adding step indicator to table.");
                        }
                        _step.add(iid);

                        JobDetails we = new JobDetails();
                        we.setInstanceId(iid);
                        we.setType(JobType.RESUME);
                        _process._engine._contexts.scheduler.schedulePersistedJob(we, null);

                        return true;
                    }
                    return false;
                }
            });

        } catch (InstanceNotFoundException infe) {
            throw infe;
        } catch (Exception ex) {
            __log.error("UnexpectedEx", ex);
            throw new RuntimeException(ex);
        }

        return doit;
    }

    /**
     * Process BPEL events WRT debugging.
     * @param event BPEL event
     */
    public void onEvent(BpelEvent event) {

        if(_enabled && (event instanceof ProcessInstanceEvent) &&
                // I have this excluded since we are recursing here when onEvent()
                // is called from DebugSupport codepath's which change state
                !(event instanceof ProcessInstanceStateChangeEvent)) {

            final ProcessInstanceEvent evt = (ProcessInstanceEvent)event;

            //
            // prevent leaking of memory
            //
            if(evt instanceof ProcessCompletionEvent ||
                    evt instanceof ProcessTerminationEvent) {
                _step.remove(evt.getProcessInstanceId());
                _instanceBreakPoints.remove(evt.getProcessInstanceId());
                return;
            }

            boolean suspend = checkStep(evt);
            if (!suspend) {
                suspend = checkBreakPoints(evt, _globalBreakPoints);
            }
            if (!suspend){
                Breakpoint[] bp = _instanceBreakPoints.get(evt.getProcessInstanceId());
                if(bp != null) {
                    suspend = checkBreakPoints(evt, bp);
                }
            }

            if(suspend){
                _step.remove(evt.getProcessInstanceId());
                try {
                    ProcessDAO process = _db.getProcessDAO();
                    ProcessInstanceDAO instance = process.getInstance(evt.getProcessInstanceId());
                    if(ProcessState.canExecute(instance.getState())){
                        // send event
                        ProcessInstanceStateChangeEvent changeEvent = new ProcessInstanceStateChangeEvent();
                        changeEvent.setOldState(instance.getState());
                        instance.setState(ProcessState.STATE_SUSPENDED);
                        changeEvent.setNewState(ProcessState.STATE_SUSPENDED);
                        changeEvent.setProcessInstanceId(instance.getInstanceId());

                        changeEvent.setProcessName(process.getType());
                        changeEvent.setProcessId(_db.getProcessId());

                        _process.saveEvent(changeEvent, instance);
                        onEvent(changeEvent);
                    }
                } catch (Exception dce) {
                    __log.error(__msgs.msgDbError(), dce);
                }
            }
        }
    }

    private boolean checkStep(ProcessInstanceEvent event){
        Long pid = event.getProcessInstanceId();
        return (_step.contains(pid)
                && (event instanceof ActivityExecStartEvent
                || event instanceof ScopeCompletionEvent));
    }

    private boolean checkBreakPoints(ProcessInstanceEvent event, Breakpoint[] breakpoints){
        boolean suspended = false;
        for(int i = 0; i < breakpoints.length; ++i){
            if (((BreakpointImpl)breakpoints[i]).checkBreak(event)){
                suspended = true;
                break;
            }
        }
        return suspended;
    }

    public boolean resume(final Long iid) {
        boolean doit = false;

        try {
            doit = _db.exec(new BpelDatabase.Callable<Boolean>() {
                public Boolean run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    if (instance == null)
                        throw new InstanceNotFoundException("" + iid);

                    if(ProcessState.STATE_SUSPENDED == instance.getState()){
                        // send event
                        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
                        evt.setOldState(ProcessState.STATE_SUSPENDED);
                        short previousState = instance.getPreviousState();

                        instance.setState(previousState);

                        evt.setNewState(previousState);
                        evt.setProcessInstanceId(iid);
                        evt.setProcessName(instance.getProcess().getType());
                        evt.setProcessId(_db.getProcessId());
                        _process.saveEvent(evt, instance);
                        onEvent(evt);

                        JobDetails we = new JobDetails();
                        we.setType(JobType.RESUME);
                        we.setInstanceId(iid);
                        _process._engine._contexts.scheduler.schedulePersistedJob(we, null);


                        return true;
                    }
                    return false;
                }
            });

        } catch (InstanceNotFoundException infe) {
            throw infe;
        } catch (Exception ex) {
            __log.error("ProcessingEx", ex);
            throw new ProcessingException(ex.getMessage(),ex);
        }

        return doit;
    }

    public void suspend(final Long iid) {

        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    if (instance == null) {
                        throw new InstanceNotFoundException("" + iid);
                    }
                    if (ProcessState.canExecute(instance.getState())) {
                        // send event
                        ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
                        evt.setOldState(instance.getState());
                        instance.setState(ProcessState.STATE_SUSPENDED);
                        evt.setNewState(ProcessState.STATE_SUSPENDED);
                        evt.setProcessInstanceId(iid);
                        ProcessDAO process = instance.getProcess();
                        evt.setProcessName(process.getType());
                        evt.setProcessId(process.getProcessId());
                        _process.saveEvent(evt, instance);
                        onEvent(evt);
                    }
                    return null;
                }
            });
        } catch (ManagementException me) {
            throw me;
        } catch (Exception ex) {
            __log.error("DbError", ex);
            throw new RuntimeException(ex);
        }

    }

    public void terminate(final Long iid) {
        try {
            _db.exec(new BpelDatabase.Callable<Object>() {
                public Object run(BpelDAOConnection conn) throws Exception {
                    ProcessInstanceDAO instance = conn.getInstance(iid);
                    if (instance == null)
                        throw new ManagementException("InstanceNotFound:" + iid);
                    // send event
                    ProcessInstanceStateChangeEvent evt = new ProcessInstanceStateChangeEvent();
                    evt.setOldState(instance.getState());
                    instance.setState(ProcessState.STATE_TERMINATED);
                    evt.setNewState(ProcessState.STATE_TERMINATED);
                    evt.setProcessInstanceId(iid);
                    ProcessDAO process = instance.getProcess();
                    QName processName = process.getType();
                    evt.setProcessName(processName);
                    QName processId = process.getProcessId();
                    evt.setProcessId(processId);
                    _process.saveEvent(evt, instance);
                    //
                    // TerminationEvent (peer of ProcessCompletionEvent)
                    //
                    ProcessTerminationEvent terminationEvent =
                            new ProcessTerminationEvent();
                    terminationEvent.setProcessInstanceId(iid);
                    terminationEvent.setProcessName(processName);
                    terminationEvent.setProcessId(processId);
                    _process.saveEvent(evt, instance);

                    onEvent(evt);
                    onEvent(terminationEvent);

                    return null;
                }
            });
        } catch (ManagementException me) {
            throw me;
        } catch (Exception e) {
            __log.error("DbError", e);
            throw new RuntimeException(e);
        }


    }

    /**
     * @return the process model. Currently an {@link OProcess}
     * However it is not guaranteed that it will remain an OProcess
     * in future versions of ODE or for different types
     * of process lanaguage than BPEL.
     */
    public Object getProcessModel() {
        return _process.getOProcess();
    }
}
