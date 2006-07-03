/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package test.pxe;

import com.fs.pxe.bpel.common.ProcessState;

import junit.framework.Assert;


/**
 *
 * @author mstevens
 */
public class StepCreateInstanceReceive extends Base {
  
  public StepCreateInstanceReceive(String testName) {
    super(testName);
  }

  protected void setUp() throws Exception {
    super.setUp();
    setUpHelloNative();     
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

	public void test() throws Exception {
    BpelManagementHelper im = getHelloNativeBpelManagement();
    
    Long newPiid = trapNewHelloNativeInstance(im, "start");
    
    //
    // STEP 1
    //
    im.fascade.step(newPiid);
    System.out.println("Waiting 2 second for new process instance to step");
    Thread.sleep(2000);

    short state = im.fascade.getState(newPiid);
    
    if(ProcessState.STATE_SUSPENDED == state) {
      im.fascade.resume(newPiid);
      System.out.println("Waiting 5 seconds for new process instance to resume");
      Thread.sleep(5000);
    }
    
    state = im.fascade.getState(newPiid);
    
    System.out.println("new PIID=" + newPiid + " state=" + state);
    
    Assert.assertEquals("process instance state should be STATE_COMPLETED_OK",
      state,ProcessState.STATE_COMPLETED_OK);
	}

}
