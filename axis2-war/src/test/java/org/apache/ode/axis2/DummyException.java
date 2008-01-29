package org.apache.ode.axis2;

/**
 * @author Matthieu Riou <mriou@apache.org>
 */
public class DummyException extends Exception {
    String reason = "dummyReason";

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
