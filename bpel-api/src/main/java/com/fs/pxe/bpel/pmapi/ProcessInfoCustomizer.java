package com.fs.pxe.bpel.pmapi;

import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * Used to customize the response document provided by most methods returning
 * process info.
 */
public class ProcessInfoCustomizer {

  public static final ProcessInfoCustomizer ALL = new ProcessInfoCustomizer(Item.ENDPOINTS,Item.PROPERTIES,Item.SUMMARY);
  public static final ProcessInfoCustomizer NONE = new ProcessInfoCustomizer();

  private HashSet<Item> _includes = new HashSet<Item>();
  
  public ProcessInfoCustomizer(String value) {
    StringTokenizer stok = new StringTokenizer(value,",",false);
    while (stok.hasMoreTokens()) {
      String t = stok.nextToken();
      Item i = Item.valueOf(t);
      _includes.add(i);
    }
  }
  
  public String toString() {
    StringBuilder buf = new StringBuilder();
    boolean first = false;
    for (Item i : _includes) {
      if (first)
        first = false;
      else
        buf.append(',');
      buf.append(i.toString());
    }
    return buf.toString();
  }
  
  public ProcessInfoCustomizer(Item... items) {
    for (Item i : items)
      _includes.add(i);
  }

  public boolean includeInstanceSummary() {
    return _includes.contains(Item.SUMMARY);
  }

  public boolean includeProcessProperties() {
    return _includes.contains(Item.PROPERTIES);
  }

  public boolean includeEndpoints() {
    return _includes.contains(Item.ENDPOINTS);
  }
  
  public enum Item {
    SUMMARY,
    PROPERTIES,
    ENDPOINTS
  }

}
