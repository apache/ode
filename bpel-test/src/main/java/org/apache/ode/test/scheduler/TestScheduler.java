package org.apache.ode.test.scheduler;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.ode.bpel.iapi.ContextException;
import org.apache.ode.bpel.iapi.Scheduler;

public class TestScheduler implements Scheduler {

	public String schedulePersistedJob(Map<String, Object> arg0, Date arg1)
			throws ContextException {
		return null;
	}

	public String scheduleVolatileJob(boolean arg0, Map<String, Object> arg1,
			Date arg2) throws ContextException {
		return null;
	}

	public void cancelJob(String arg0) throws ContextException {

	}

	public <T> T execTransaction(Callable<T> arg0) throws Exception,
			ContextException {
		T retval = arg0.call();
		return retval;
	}

	public void start() {
	}

	public void stop() {
	}

}
