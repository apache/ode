package org.apache.ode.bpel.compiler.bom;

import javax.xml.namespace.QName;


public abstract class ExtensibilityQNames {
    /**
     * Activity Recovery extensibility elements.
     */
    public static final String NS_ACTIVITY_RECOVERY         = "http://ode.apache.org/activityRecovery";
    public static final QName FAILURE_HANDLING              = new QName(NS_ACTIVITY_RECOVERY, "failureHandling");
    public static final QName FAILURE_HANDLING_RETRY_FOR    = new QName(NS_ACTIVITY_RECOVERY, "retryFor");
    public static final QName FAILURE_HANDLING_RETRY_DELAY  = new QName(NS_ACTIVITY_RECOVERY, "retryDelay");
    public static final QName FAILURE_HANDLING_FAULT_ON     = new QName(NS_ACTIVITY_RECOVERY, "faultOnFailure");

}

