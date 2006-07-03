/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.engine;

import com.fs.pxe.bpel.bdi.breaks.Breakpoint;
import com.fs.pxe.bpel.common.ProcessState;
import com.fs.pxe.bpel.dao.BpelDAOConnection;
import com.fs.pxe.bpel.dao.ProcessDAO;
import com.fs.pxe.bpel.dao.ProcessInstanceDAO;
import com.fs.pxe.bpel.evt.*;
import com.fs.pxe.bpel.pmapi.BpelManagementFacade;
import com.fs.pxe.bpel.pmapi.InstanceNotFoundException;
import com.fs.pxe.bpel.pmapi.ManagementException;
import com.fs.pxe.bpel.pmapi.ProcessingException;
import com.fs.pxe.bpel.runtime.BpelEventListener;
import com.fs.pxe.bpel.runtime.breaks.BreakpointImpl;
import com.fs.utils.ArrayUtils;
import com.fs.utils.msg.MessageBundle;

import java.util.*;

import javax.xml.namespace.QName;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class providing functions used to support debugging funtionality
 * in the BPEL engine. This class serves as the underlying
 * implementation of the {@link BpelManagementFacade} interface, and
 * the various MBean interfaces.
 *
 * @todo Need to revisit the whole stepping/suspend/resume mechanism.
 */
class DebuggerSupport implements BpelEventListener {

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
  DebuggerSupport(BpelProcess process) {
    _process = process;
    _db = new BpelProcessDatabase(_process._engine._contexts.dao,
        _process._engine._contexts.scheduler,
        _process._pid);
    
	}
  
  void enable(boolean enabled){
  	_enabled = enabled;
  }
  
  Breakpoint[] getGlobalBreakpoints(){
  	return _globalBreakPoints;
  }
  
  Breakpoint[] getBreakpoints(Long pid){
  	Breakpoint[] arr = _instanceBreakPoints.get(pid);
    return (arr == null)
      ? EMPTY_BP
      : arr;
  }
  
  void addGlobalBreakpoint(Breakpoint breakpoint){
  	Collection<Breakpoint> c = ArrayUtils.makeCollection(ArrayList.class, _globalBreakPoints);
    c.add(breakpoint);
    _globalBreakPoints = c.toArray(new Breakpoint[c.size()]);
  }
  
  void addBreakpoint(Long pid, Breakpoint breakpoint){
  	Breakpoint[] bpArr = _instanceBreakPoints.get(pid);
    if(bpArr == null) {
      bpArr = new Breakpoint[]{breakpoint};
    }
    else{
      Collection<Breakpoint> c = ArrayUtils.makeCollection(ArrayList.class, bpArr);
      c.add(breakpoint);
      bpArr = c.toArray(new Breakpoint[c.size()]);
    }
    _instanceBreakPoints.put(pid, bpArr);
  }
  
  void removeGlobalBreakpoint(Breakpoint breakpoint){
  	Collection<Breakpoint> c = ArrayUtils.makeCollection(ArrayList.class, _globalBreakPoints);
    c.remove(breakpoint);
    _globalBreakPoints = c.toArray(new Breakpoint[c.size()]);
  }
  
  void removeBreakpoint(Long pid, Breakpoint breakpoint){
  	Breakpoint[] bpArr = _instanceBreakPoints.get(pid);
    if(bpArr != null){
      Collection<Breakpoint> c = ArrayUtils.makeCollection(ArrayList.class, bpArr);
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
            evt.setProcessName(instance.getProcess().getDefinitionName());
            evt.setProcessId(_db.getProcessId());
              
            instance.insertBpelEvent(evt);
            
            onEvent(evt);
            
            __log.debug("step(" + iid + ") adding step indicator to table.");
            _step.add(iid);
            
            WorkEvent we = new WorkEvent();
            we.setIID(iid);
            we.setType(WorkEvent.Type.RESUME);
            _process._engine._contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
            
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
        __log.debug("onEvent(" + evt.getProcessInstanceId() + ") Cleaning up step indicator and instance breakpoints");
        _step.remove(evt.getProcessInstanceId());
        _instanceBreakPoints.remove(evt.getProcessInstanceId());
        return;
      }

      boolean suspend = checkStep(evt);
      if (!suspend) {
        __log.debug("onEvent(" + evt.getProcessInstanceId() + ") Checking global breakpoints");
      	suspend = checkBreakPoints(evt, _globalBreakPoints);
      }
      if (!suspend){
      	Breakpoint[] bp = _instanceBreakPoints.get(evt.getProcessInstanceId());
        if(bp != null) {
          __log.debug("onEvent(" + evt.getProcessInstanceId() + ") Checking instance breakpoints");
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
            
            changeEvent.setProcessName(process.getDefinitionName());
            changeEvent.setProcessId(_db.getProcessId());
            
            instance.insertBpelEvent(changeEvent);
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
    __log.debug("checkStep(" + event.getProcessInstanceId() + ") Event is type " + event.getClass().getName());
    if(_step.contains(pid))
    __log.debug("checkStep(" + event.getProcessInstanceId() + ") Step indication found");
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
            evt.setProcessName(instance.getProcess().getDefinitionName());
            evt.setProcessId(_db.getProcessId());
            instance.insertBpelEvent(evt);
            onEvent(evt);

            WorkEvent we = new WorkEvent();
            we.setType(WorkEvent.Type.RESUME);
            we.setIID(iid);
            _process._engine._contexts.scheduler.schedulePersistedJob(we.getDetail(), null);
            
                
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
            evt.setProcessName(process.getDefinitionName());
            evt.setProcessId(process.getProcessId());
            instance.insertBpelEvent(evt);
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
          QName processName = process.getDefinitionName();
          evt.setProcessName(processName);
          QName processId = process.getProcessId();
          evt.setProcessId(processId);
          instance.insertBpelEvent(evt);
          //
          // TerminationEvent (peer of ProcessCompletionEvent)
          //
          ProcessTerminationEvent terminationEvent =
            new ProcessTerminationEvent();
          terminationEvent.setProcessInstanceId(iid);
          terminationEvent.setProcessName(processName);
          terminationEvent.setProcessId(processId);
          instance.insertBpelEvent(terminationEvent);

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
}
