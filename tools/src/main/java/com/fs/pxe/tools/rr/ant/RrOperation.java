/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.tools.rr.ant;

import com.fs.pxe.sfwk.rr.ResourceRepositoryBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Simple common interface for RR operations.
 */
public interface RrOperation {

  public void execute(RrTask executingTask, ResourceRepositoryBuilder rrb)
    throws URISyntaxException, IOException;

}

