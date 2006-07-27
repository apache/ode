package com.fs.pxe.jca.server;

public interface ConnectionProvider {
  String[] getConnectionIntefaces();
  
  Object createConnectionObject();
  
  void destroyConnectionObject(Object cobj);
    

}
