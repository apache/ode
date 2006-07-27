package org.apache.ode.ql.tree;

import org.apache.ode.ql.jcc.TreeBuilder;

public class BuilderFactory {
  private static BuilderFactory INSTANCE = new BuilderFactory();
  
  private BuilderFactory() {}
  
  public static BuilderFactory getInstance() {
    return INSTANCE;
  }
  
  public Builder createBuilder() {
    return new TreeBuilder();
  }
}
