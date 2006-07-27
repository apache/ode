/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package com.fs.jacob.soup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * DOCUMENTME.
 * 
 * <p>
 * Created on Feb 16, 2004 at 9:13:39 PM.
 * </p>
 * 
 * @author Maciej Szefler <a href="mailto:mbs@fivesight.com">mbs</a>
 * 
 */

public class CommGroup extends SoupObject {

  boolean _isReplicated;
  List<Comm> _comms = new ArrayList<Comm>();

  public CommGroup(boolean replicated) {
    _isReplicated = replicated;
  }

  /**
   * Read the value of the replication operator flag. CommRecv (channel reads)
   * with the replication flag set are left in the queue indefinately.
   * 
   * @return true or false
   */
  public boolean isReplicated() {
    return _isReplicated;
  }

  public void add(Comm comm) {
    comm.setGroup(this);
    _comms.add(comm);
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    for (Iterator<Comm> i = _comms.iterator(); i.hasNext();) {
      buf.append(i.next());
      if (i.hasNext()) buf.append(" + ");
    }

    return buf.toString();
  }

  public String getDescription() {
    return toString();
  }

  public Iterator<Comm> getElements() {
    return _comms.iterator();
  }

}
