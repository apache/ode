package com.fs.pxe.ql.tree;

import com.fs.pxe.ql.jcc.TreeBuilder;

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
