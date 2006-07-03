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
public class AsyncStepTest extends Base {
  
  public AsyncStepTest(String testName) {
    super(testName);
  }

  protected void setUp() throws Exception {
    super.setUp();
    setUpAsyncNative();     
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

	public void test() throws Exception {
    BpelManagementHelper im = getAsyncNativeBpelManagement();
    
    Long newPiid = trapNewAsyncNativeInstance(im,
      false, // just one Order (will be for a BookOrder)
      "invoke_BookOrder"); // callback is "onMessage_Callback"
    
    //
    // STEP 1
    //
    im.fascade.step(newPiid);
    System.out.println("Waiting 3 seconds to step over invoke_BookOrder to Pick");
    Thread.sleep(3000);

    short state = im.fascade.getState(newPiid);
    
    if(ProcessState.STATE_SUSPENDED == state) {
      //
      // STEP 2
      //
      im.fascade.step(newPiid);
      System.out.println("Waiting 5 second to step over pick");
      Thread.sleep(5000);
      
      state = im.fascade.getState(newPiid);
        
      if(ProcessState.STATE_SUSPENDED == state) {
        im.fascade.resume(newPiid);
        System.out.println("Waiting 9 seconds for new process instance to complete after resume");
        Thread.sleep(9000);
      }
    }
    
    state = im.fascade.getState(newPiid);
    
    System.out.println("new PIID=" + newPiid + " state=" + state);
    
    Assert.assertEquals("process instance state should be STATE_COMPLETED_OK",
      state,ProcessState.STATE_COMPLETED_OK);
	}

}
