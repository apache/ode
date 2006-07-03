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
public class KillTest extends Base {
  
  public KillTest(String testName) {
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
    
    Long newPiid = trapNewHelloNativeInstance(im, "assign1");
    
    im.fascade.terminate(newPiid);
    
    short state = im.fascade.getState(newPiid);
    
    System.out.println("new PIID=" + newPiid + " state=" + state);
    
    Assert.assertEquals("process instance state should be STATE_TERMINATED",
      state,ProcessState.STATE_TERMINATED);
	}

}
