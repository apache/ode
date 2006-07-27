package com.fs.pxe.ra.transports.rmi;

import com.fs.pxe.ra.transports.PxeTransportPipe;

import java.rmi.Remote;

/**
 * RMI interface for a remote PXE connection listener.
 */
public interface PxeTransportPipeRemote extends Remote, PxeTransportPipe {
}
