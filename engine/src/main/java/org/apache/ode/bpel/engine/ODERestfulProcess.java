package org.apache.ode.bpel.engine;

public class ODERestfulProcess extends ODEProcess {


    // Restful processes don't lazy load their OModel, they need it right away
    protected void latch(int s) { }
    protected void releaseLatch(int s) { }
    protected boolean isLatched(int s) { return false; }
}
