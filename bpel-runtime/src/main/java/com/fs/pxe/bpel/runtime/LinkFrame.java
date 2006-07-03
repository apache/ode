/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.runtime;

import com.fs.pxe.bpel.o.OLink;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Link stack frame allowing resolution of {@link OLink} objects to the
 * current {@link LinkInfo} in context.
 */
class LinkFrame implements Serializable {

	private static final long serialVersionUID = 1L;
	LinkFrame next;
  Map<OLink, LinkInfo> links = new HashMap<OLink, LinkInfo>();

  LinkFrame(LinkFrame next) {
    this.next = next;
  }

  LinkInfo resolve(OLink link) {
    LinkInfo li = links.get(link);
    if (li == null && next != null)
      return next.resolve(link);
    return li;
  }

}
