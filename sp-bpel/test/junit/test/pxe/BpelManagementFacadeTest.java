/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package test.pxe;

import com.fs.pxe.bpel.bdi.breaks.Breakpoint;
import com.fs.pxe.bpel.common.InstanceQuery;
import com.fs.pxe.bpel.common.ProcessState;
import com.fs.pxe.bpel.evt.BpelEvent;

import java.util.List;


/**
 *
 * @author mstevens
 */
public class BpelManagementFacadeTest extends Base {
  
  public BpelManagementFacadeTest(String testName) {
    super(testName);
  }

  protected void setUp() throws Exception {
    super.setUp();
    if(!checkForSystem("HelloWorld"))
      throw new Exception("PXE System HelloWorld needs to be deployed");
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

	public void test() throws Exception {
    BpelManagementHelper im = getHelloBpelManagement();
    System.out.println("OModel: getProcessDef() " +
      im.fascade.getProcessDef(im.processID).toString());
    
    Breakpoint[] globalBreakpoints = im.fascade.getGlobalBreakpoints(im.processID);
    System.out.println("" + globalBreakpoints.length + " global breakpoints:");   
    for(Breakpoint globalBreakpoint: globalBreakpoints)
      System.out.println("   " + globalBreakpoint);
      
    
    InstanceQuery query = new InstanceQuery();
    query.processState = ProcessState.ALL_STATES;
    query.processId = im.processID;
    for(Long piid:im.fascade.getProcessInstances(query)) {
      System.out.println("PID=" + piid + ":");
      System.out.println("   startTime=" + im.fascade.getStartTime(piid));
      System.out.println("   state=" + im.fascade.getState(piid));
      
      System.out.println("   event count=" + im.fascade.getEventCount(piid));
      List<BpelEvent> result = im.fascade.getEvents(piid,0,999);
      BpelEvent[] events = result.toArray(new BpelEvent[result.size()]);
      if(events != null) {
        System.out.println("   " + events.length + " events:");   
        for(BpelEvent event: events)
          System.out.println("      " + event);
      } else
        System.out.println("   null events:");
        
      
      Breakpoint[] breakpoints = im.fascade.getBreakpoints(piid);
      if(null != breakpoints) {
        System.out.println("   " + breakpoints.length + " instance breakpoints:");   
        for(Breakpoint breakpoint: breakpoints)
          System.out.println("      " + breakpoint);
      } else
        System.out.println("   null breakpoints:");   
    }
	}

}
