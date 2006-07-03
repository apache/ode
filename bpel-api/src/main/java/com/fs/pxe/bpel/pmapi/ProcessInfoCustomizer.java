package com.fs.pxe.bpel.pmapi;

/**
 * Used to customize the response document provided by most methods returning
 * process info.
 */
public class ProcessInfoCustomizer {

  public static final ProcessInfoCustomizer ALL = new ProcessInfoCustomizer(true, true, true);
  public static final ProcessInfoCustomizer NONE = new ProcessInfoCustomizer(false, false, false);

  private boolean includeInstanceSummary;
  private boolean includeProcessProperties;
  private boolean includeEndpoints;

  public ProcessInfoCustomizer(boolean includeInstanceSummary, boolean includeProcessProperties,
                               boolean includeEndpoints) {
    this.includeInstanceSummary = includeInstanceSummary;
    this.includeProcessProperties = includeProcessProperties;
    this.includeEndpoints = includeEndpoints;
  }

  public boolean includeInstanceSummary() {
    return includeInstanceSummary;
  }

  public boolean includeProcessProperties() {
    return includeProcessProperties;
  }

  public boolean includeEndpoints() {
    return includeEndpoints;
  }

}
