/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */

package test.pxe;

import com.fs.pxe.bpel.common.InstanceQuery;
import com.fs.pxe.bpel.common.ProcessState;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

/**
 *
 * @author mstevens
 */
public class ProcessMBeanTest extends Base {
  
  public ProcessMBeanTest(String testName) {
    super(testName);
  }

  protected void setUp() throws Exception {
    super.setUp();
    setUpHello();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
  }

	public void test() throws Exception {
		InstanceQuery query = new InstanceQuery();
    query.processId = "HelloWorld.helloWorld.BpelService";
		query.processState = ProcessState.ALL_STATES;
		TabularData processInstances =
      getHelloProcessMBean().instanceQueryAdvanced(query);
		for (Object processInstance : processInstances.values()) {
			Long piid = (Long)((CompositeData)processInstance).get("PIID");
			System.out.println("PID=" + piid + ": " + ((CompositeData)processInstance).toString());
		}
	}

}
