package org.apache.ode.ra.transports.rmi;

import org.apache.ode.ra.transports.OdeTransportPipe;

import java.rmi.Remote;

/**
 * RMI interface for a remote ODE connection listener.
 */
public interface OdeTransportPipeRemote extends Remote, OdeTransportPipe {
}
