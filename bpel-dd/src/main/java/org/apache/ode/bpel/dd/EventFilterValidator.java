package org.apache.ode.bpel.dd;

import org.apache.ode.bpel.o.OBase;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.o.OScope;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates event filters declared on scopes.
 */
public class EventFilterValidator implements DDValidator {

  public void validate(TDeploymentDescriptor dd, OProcess process) throws DDValidationException {
    List<String> scopeNames = getProcessScopeNames(process);
    if (dd.getProcessEvents() != null && dd.getProcessEvents().getScopeEventsList().size() > 0) {
      for (TDeploymentDescriptor.ProcessEvents.ScopeEvents scopeEvents : dd.getProcessEvents().getScopeEventsList()) {
        if (!scopeNames.contains(scopeEvents.getName()))
          throw new DDValidationException("Event filters are declared for scope " +
                  scopeEvents.getName() + " in your deployment descriptor but this scope " +
                  "can't be found in the process definition.");
      }
    }
  }

  private List<String> getProcessScopeNames(OProcess process) {
    ArrayList<String> names = new ArrayList<String>();
    for (OBase oBase : process.getChildren()) {
      if (oBase instanceof OScope) names.add(((OScope)oBase).name);
    }
    return names;
  }
}
