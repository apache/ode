/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.ra;

import javax.resource.cci.ConnectionFactory;

/**
 * Factory for obtaining {@link PxeConnection}s: the entry point for external
 * objects needing to access the PXE domain.
 */
public interface PxeConnectionFactory extends ConnectionFactory {
}
