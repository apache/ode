package org.apache.ode.bpel.rapi;

import java.util.Date;

import org.w3c.dom.Element;

/**
 * Engine-provided methods for recovery management. 
 * 
 * @author mszefler
 *
 */
public interface RecoveryContext {
	
	void registerActivityForRecovery(String channel, long activityId,
			String reason, Date dateTime, Element details, String[] actions,
			int retries);

	void unregisterActivityForRecovery(String channel);


}
