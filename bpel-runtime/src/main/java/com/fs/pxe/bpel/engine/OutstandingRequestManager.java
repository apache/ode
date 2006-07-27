/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.pxe.bpel.engine;

import com.fs.pxe.bpel.runtime.PartnerLinkInstance;
import com.fs.pxe.bpel.runtime.Selector;
import com.fs.utils.ObjectPrinter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Manages receive/pick--reply matching.
 */
class OutstandingRequestManager implements Serializable {
  private static final long serialVersionUID = -5556374398943757951L;

  private static final Log __log = LogFactory.getLog(OutstandingRequestManager.class);

  private final Map<RequestIdTuple, Entry> _byRid = new HashMap<RequestIdTuple, Entry>();
  private final Map<String, Entry> _byChannel = new HashMap<String, Entry>();

  int findConflict(Selector selectors[]) {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("findConflict", new Object[] { "selectors", selectors}) );
    }

    Set<RequestIdTuple> workingSet = new HashSet<RequestIdTuple>(_byRid.keySet());
    for (int i = 0; i < selectors.length; ++i) {
      if (selectors[i].oneWay) {
        continue;
      }
      final RequestIdTuple rid = new RequestIdTuple(selectors[i].plinkInstance,selectors[i].opName, selectors[i].messageExchangeId);
      if (workingSet.contains(rid)) {
        return i;
      }
      workingSet.add(rid);
    }
    return -1;
  }

  void register(String pickResponseChannel, Selector selectors[]) {
    if (__log.isTraceEnabled()) {
      __log.trace(ObjectPrinter.stringifyMethodEnter("register", new Object[] {
        "pickResponseChannel", pickResponseChannel,
        "selectors", selectors
      }) );
    }

    if (_byChannel.containsKey(pickResponseChannel)) {
      String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RESPONSE CHANNEL " + pickResponseChannel;
      __log.fatal(errmsg);
      throw new IllegalArgumentException(errmsg);
    }

    Entry entry = new Entry(pickResponseChannel, selectors);
    for (int i = 0 ; i < selectors.length; ++i) {
      if (selectors[i].oneWay) {
        continue;
      }
      
      final RequestIdTuple rid = new RequestIdTuple(selectors[i].plinkInstance,selectors[i].opName, selectors[i].messageExchangeId);
      if (_byRid.containsKey(rid)) {
        String errmsg = "INTERNAL ERROR: Duplicate ENTRY for RID " + rid;
        __log.fatal(errmsg);
        throw new IllegalArgumentException(errmsg);
      }
      _byRid.put(rid,  entry);
    }

    _byChannel.put(pickResponseChannel, entry);
  }

  void cancel(String pickResponseChannel) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("cancel", new Object[] {
        "pickResponseChannel", pickResponseChannel
      }) );

    Entry entry = _byChannel.remove(pickResponseChannel);
    if (entry != null) {
      _byRid.values().remove(entry);
    }
  }

  void associate(String pickResponseChannel, String mexRef) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("associate", new Object[] {
        "pickResponseChannel", pickResponseChannel,
        "mexRef", mexRef
      }) );

    Entry entry = _byChannel.get(pickResponseChannel);
    if (entry == null) {
      String errmsg = "INTERNAL ERROR: No ENTRY for RESPONSE CHANNEL " + pickResponseChannel;
      __log.fatal(errmsg);
      throw new IllegalArgumentException(errmsg);
    }

    if (entry.mexRef != null) {
      String errmsg = "INTERNAL ERROR: Duplicate ASSOCIATION for CHANEL " + pickResponseChannel;
      __log.fatal(errmsg);
      throw new IllegalStateException(errmsg);
    }

    entry.mexRef = mexRef;
  }

  public String release(PartnerLinkInstance plinkInstnace, String opName, String mexId) {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("release", new Object[] {
        "plinkInstance", plinkInstnace,
        "opName", opName,
        "mexId", mexId
      }) );

    final RequestIdTuple rid = new RequestIdTuple(plinkInstnace,opName, mexId);
    Entry entry = _byRid.get(rid);
    if (entry == null) {
      if (__log.isDebugEnabled()) {
        __log.debug("==release: RID " + rid + " not found in " + _byRid);
      }
      return null;
    }
    while(_byChannel.values().remove(entry));
    while(_byRid.values().remove(entry));
    return entry.mexRef;
  }

  public String[] releaseAll() {
    if (__log.isTraceEnabled())
      __log.trace(ObjectPrinter.stringifyMethodEnter("releaseAll", null) );

    int idx = 0;
    String[] mexRefs = new String[_byChannel.size()];
    for (Entry entry : _byChannel.values()) {
      mexRefs[idx++] = entry.mexRef;
    }
    _byChannel.values().clear();
    _byRid.values().clear();
    return mexRefs;
  }

  public String toString() {
    return ObjectPrinter.toString(this, new Object[] {
      "byRid", _byRid,
      "byChannel", _byChannel
    });
  }

  /**
   * Tuple identifying an outstanding request (i.e. a receive,pick, or onMessage on a
   * synchronous operation needing a reply).
   */
  private class RequestIdTuple  implements Serializable {
    private static final long serialVersionUID = -1059389611839777482L;
    /** On which partner link it was received. */
    PartnerLinkInstance partnerLink;
    /** Name of the operation. */
    String opName;
    /** Message exchange identifier. */
    String mexId;

    /** Constructor. */
    private RequestIdTuple(PartnerLinkInstance partnerLink, String opName, String mexId) {
      this.partnerLink = partnerLink;
      this.opName = opName;
      this.mexId = mexId == null ? "" : mexId;
    }

    public int hashCode() {
      return this.partnerLink.hashCode() ^ this.opName.hashCode() ^ this.mexId.hashCode();
    }

    public boolean equals(Object obj) {
      RequestIdTuple other = (RequestIdTuple) obj;
      return other.partnerLink.equals(partnerLink) &&
              other.opName.equals(opName) &&
              other.mexId.equals(mexId);
    }

    public String toString() {
      return ObjectPrinter.toString(this, new Object[] {
        "partnerLink", partnerLink,
        "opName", opName,
        "mexId", mexId
      });
    }
  }

  private class Entry implements Serializable {
    private static final long serialVersionUID = -583743124656582887L;
    final String pickResponseChannel;
    final Selector[] selectors;
    String mexRef;

    private Entry(String pickResponseChannel, Selector[] selectors) {
      this.pickResponseChannel = pickResponseChannel;
      this.selectors = selectors;
    }

    public String toString() {
      return ObjectPrinter.toString(this, new Object[] {
        "pickResponseChannel", pickResponseChannel,
        "selectors", selectors,
        "mexRef", mexRef
      });
    }
  }
}
