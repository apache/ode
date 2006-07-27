/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.ra;

import javax.resource.cci.ConnectionFactory;

/**
 * Factory for obtaining {@link OdeConnection}s: the entry point for external
 * objects needing to access the ODE domain.
 */
public interface OdeConnectionFactory extends ConnectionFactory {
}
