/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.bpel.o.OLink;
import org.apache.ode.bpel.runtime.channels.LinkStatusChannel;

import java.io.Serializable;

/**
 * Run-time represetation of the link data.
 */
class LinkInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	final OLink olink;

  /** Channel to be used for link status publisher. */
  final LinkStatusChannel pub;

  /** Channel to be used for link status listener. */
  final LinkStatusChannel sub;


  LinkInfo(OLink olink, LinkStatusChannel pub, LinkStatusChannel sub) {
    this.olink = olink;
    this.pub = pub;
    this.sub = sub;
  }

}
