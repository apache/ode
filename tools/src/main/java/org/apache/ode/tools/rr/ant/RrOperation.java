/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.tools.rr.ant;

import org.apache.ode.utils.rr.ResourceRepositoryBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Simple common interface for RR operations.
 */
public interface RrOperation {

  public void execute(RrTask executingTask, ResourceRepositoryBuilder rrb)
    throws URISyntaxException, IOException;

}

