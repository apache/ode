/*
 * File:      $RCSfile$
 * Copyright: (C) 1999-2005 FiveSight Technologies Inc.
 *
 */
package genbuild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


/**
 * enclosing_type class/interface.  DOCUMENT_ME
 */
public class Workspace {

  //~ Instance/static variables ...............................................

  private HashMap _modules = new HashMap();

  //~ Constructors ............................................................

  /**
   * Constructor for Workspace.
   */
  public Workspace() {
    super();
  }

  //~ Methods .................................................................

  /**
   * Method getLibDependencies.
   * 
   * @param module
   * @return Collection
   * @throws WorkspaceException DOCUMENTME
   */
  public Collection getLibDependencies(String module)
        throws WorkspaceException {

    ModuleInfo mi = (ModuleInfo)_modules.get(module);

    if (mi == null)
      throw new WorkspaceException("module not found: " + module);

    return mi.ldependencies;
  }

  /**
   * DOCUMENTME
   * 
   * @return DOCUMENTME
   */
  public Collection getModules() {

    return _modules.keySet();
  }

  /**
   * DOCUMENTME
   * 
   * @param module DOCUMENTME
   * @return DOCUMENTME
   * @throws WorkspaceException DOCUMENTME
   */
  public Collection getRequiredModules(String module)
        throws WorkspaceException {

    ModuleInfo mi = (ModuleInfo)_modules.get(module);

    if (mi == null)
      throw new WorkspaceException("module not found: " + module);

    return mi.pdependencies;
  }

  /**
   * DOCUMENTME
   * 
   * @param module DOCUMENTME
   * @param lib DOCUMENTME
   * @throws WorkspaceException DOCUMENTME
   */
  public void addLibDependency(String module, String lib)
        throws WorkspaceException {

    ModuleInfo mi = (ModuleInfo)_modules.get(module);

    if (mi == null)
      throw new WorkspaceException("module not found: " + module);

    mi.ldependencies.add(lib);
  }

  /**
   * DOCUMENTME
   * 
   * @param module DOCUMENTME
   * @throws WorkspaceException DOCUMENTME
   */
  public void addModule(String module)
        throws WorkspaceException {

    ModuleInfo mi = new ModuleInfo();

    if (_modules.containsKey(module))
      throw new WorkspaceException("duplicate module: " + module);

    mi.name = module;
    _modules.put(module, mi);
  }

  /**
   * DOCUMENTME
   * 
   * @param module DOCUMENTME
   * @param dependency DOCUMENTME
   * @throws WorkspaceException DOCUMENTME
   */
  public void addModuleDependency(String module, String dependency)
        throws WorkspaceException {

    ModuleInfo mi = (ModuleInfo)_modules.get(module);

    if (mi == null)
      throw new WorkspaceException("Module not found: " + module);

    ModuleInfo dep = (ModuleInfo)_modules.get(dependency);

    if (dep == null)
      throw new WorkspaceException("Module '" + module
                                   + "' depends on unavailable module '"
                                   + dependency + "'.");

    mi.pdependencies.add(dependency);
  }

  //~ Inner classes ...........................................................

  private class ModuleInfo {

    //~ Instance/static variables .............................................

    /** List of <code>Dependency</code> objects. */
    ArrayList pdependencies = new ArrayList();
    ArrayList ldependencies = new ArrayList();
    ArrayList products = new ArrayList();
    String name;
  }
}